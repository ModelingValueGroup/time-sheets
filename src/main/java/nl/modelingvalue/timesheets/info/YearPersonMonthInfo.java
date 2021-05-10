package nl.modelingvalue.timesheets.info;

import static java.util.Collections.emptyMap;
import static nl.modelingvalue.timesheets.info.DetailInfo.EMPTY_DETAIL;
import static nl.modelingvalue.timesheets.util.LogAccu.info;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.util.U;

public class YearPersonMonthInfo extends Info {
    private final Map<Integer, PersonMonthInfo> map = new HashMap<>();

    public YearPersonMonthInfo() {
    }

    public void add(Collection<YearBudgetInfo> budgets) {
        budgets.forEach(ybi -> ybi.values()
                .forEach(pbi -> IntStream.rangeClosed(1, 12)
                        .forEach(month -> add(pbi.personInfo, ybi.year, month, new DetailInfo(0, U.secFromHours(pbi.months[month - 1]))))));
        map.values().forEach(PersonMonthInfo::determineHasBudget);
    }

    public synchronized void add(YearPersonMonthInfo other) {
        other.map.forEach((year, ami) -> map.computeIfAbsent(year, ab -> new PersonMonthInfo()).add(ami));
    }

    public synchronized void add(PersonInfo person, int year, int month, DetailInfo detail) {
        map.computeIfAbsent(year, ab -> new PersonMonthInfo()).add(person, month, detail);
    }

    public Stream<Integer> getYears() {
        return map.keySet().stream();
    }

    public List<PersonInfo> getPersonInfos(int year) {
        return map.getOrDefault(year, PersonMonthInfo.EMPTY).keySet().stream().toList();
    }

    public long secFor(int year, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, PersonMonthInfo.EMPTY).secFor(f);
    }

    public long secFor(int year, int month, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, PersonMonthInfo.EMPTY).secFor(month, f);
    }

    public long secFor(PersonInfo personInfo, int year, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, PersonMonthInfo.EMPTY).secFor(personInfo, f);
    }

    public long secFor(PersonInfo personInfo, int year, int month, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, PersonMonthInfo.EMPTY).secFor(personInfo, month, f);
    }

    public boolean notEmpty(int year) {
        return secFor(year, d -> d.secWorked() + d.secBudget()) != 0;
    }

    public boolean hasBudget(int year) {
        return map.getOrDefault(year, PersonMonthInfo.EMPTY).hasBudget;
    }

    public void budgetStatusLog(String name) {
        List<Integer> entries = map.entrySet().stream().filter(e -> e.getValue().hasBudget).map(Entry::getKey).sorted(Comparator.reverseOrder()).toList();
        if (!entries.isEmpty()) {
            info(String.format("detected budgets for %-4s in %s", name, entries));
        }
    }

    private static class PersonMonthInfo extends HashMap<PersonInfo, Map<Integer, DetailInfo>> {
        private static final PersonMonthInfo EMPTY = new PersonMonthInfo();

        private boolean hasBudget;

        public void add(PersonMonthInfo other) {
            other.forEach((person, mm) -> mm.forEach((month, detail) -> add(person, month, detail)));
            hasBudget &= other.hasBudget;
        }

        public void add(PersonInfo person, int month, DetailInfo detail) {
            Map<Integer, DetailInfo> m2         = computeIfAbsent(person, y -> new HashMap<>());
            DetailInfo               detailInfo = m2.computeIfAbsent(month, m -> new DetailInfo());
            detailInfo.add(detail);
        }

        public long secFor(ToLongFunction<DetailInfo> f) {
            return values()
                    .stream()
                    .flatMapToLong(
                            mm -> mm.values()
                                    .stream()
                                    .mapToLong(f)
                    ).sum();
        }

        public long secFor(int month, ToLongFunction<DetailInfo> f) {
            return values()
                    .stream()
                    .mapToLong(
                            mm -> f.applyAsLong(mm.getOrDefault(month, EMPTY_DETAIL))
                    ).sum();
        }

        public long secFor(PersonInfo personInfo, ToLongFunction<DetailInfo> f) {
            return getOrDefault(personInfo, emptyMap())
                    .values()
                    .stream()
                    .mapToLong(f)
                    .sum();
        }

        public long secFor(PersonInfo personInfo, int month, ToLongFunction<DetailInfo> f) {
            return f.applyAsLong(getOrDefault(personInfo, emptyMap()).getOrDefault(month, EMPTY_DETAIL));
        }

        public void determineHasBudget() {
            hasBudget = values().stream().anyMatch(mm -> mm.values().stream().anyMatch(d -> 0 < d.secBudget()));
        }
    }
}
