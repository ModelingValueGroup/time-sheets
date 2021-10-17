package nl.modelingvalue.timesheets.info;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import nl.modelingvalue.timesheets.util.*;

import static java.util.Collections.*;
import static nl.modelingvalue.timesheets.info.DetailInfo.*;

public class YearPersonMonthInfo extends Info {
    private final PersonMonthInfo               EMPTY = new PersonMonthInfo(this);
    private final PGInfo                        pgInfo;
    private final List<PGInfo>                  subs  = new ArrayList<>();
    private final Map<Integer, PersonMonthInfo> map   = new HashMap<>();

    public YearPersonMonthInfo(PGInfo pgInfo) {
        this.pgInfo = pgInfo;
    }

    public void add(Collection<YearBudgetInfo> budgets) {
        budgets.forEach(ybi -> ybi.values()
                .forEach(pbi -> IntStream.rangeClosed(1, 12)
                        .forEach(month -> add(pbi.personInfo, ybi.year, month, new DetailInfo(0, U.secFromHours(pbi.months[month - 1]))))));
        map.values().forEach(PersonMonthInfo::determineHasBudget);
    }

    public synchronized void add(PGInfo pi) {
        subs.add(pi);
        pi.yearPersonMonthInfo.map.forEach((year, ami) -> map.computeIfAbsent(year, ab -> new PersonMonthInfo(this)).add(ami));
    }

    public synchronized void add(PersonInfo person, int year, int month, DetailInfo detail) {
        map.computeIfAbsent(year, ab -> new PersonMonthInfo(this)).add(person, month, detail);
    }

    public Stream<Integer> getYears() {
        return map.keySet().stream();
    }

    public Stream<PersonInfo> getPersonInfoStream(int year) {
        return map.getOrDefault(year, EMPTY).keySet().stream().sorted();
    }

    public long secFor(int year, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, EMPTY).secFor(f);
    }

    public long secFor(int year, int month, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, EMPTY).secFor(month, f);
    }

    public long secFor(PersonInfo personInfo, int year, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, EMPTY).secFor(personInfo, f);
    }

    public long secFor(PersonInfo personInfo, int year, int month, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(year, EMPTY).secFor(personInfo, month, f);
    }

    public Map<String, Long> allSecFor(PersonInfo personInfo, int year, int month, ToLongFunction<DetailInfo> f) {

        Set<PGInfo> allSubs = new HashSet<>();
        allSubs.add(pgInfo);
        allSubs.addAll(subs);

        //noinspection StatementWithEmptyBody
        while (allSubs.addAll(allSubs.stream().flatMap(pi -> pi.yearPersonMonthInfo.subs.stream()).toList())) {
        }
        if (allSubs.size() == 1 && allSubs.iterator().next() == pgInfo) {
            return null;
        }
        return allSubs.stream()
                .filter(pi -> pi instanceof ProjectInfo)
                .collect(Collectors.toMap(pi -> pi.id, pi -> pi.yearPersonMonthInfo.secFor(personInfo, year, month, f)));
    }

    public boolean notEmpty(int year) {
        return secFor(year, d -> d.secWorked() + d.secBudget()) != 0;
    }

    public boolean hasBudget(int year) {
        return map.getOrDefault(year, EMPTY).hasBudget();
    }

    private static class PersonMonthInfo extends HashMap<PersonInfo, Map<Integer, DetailInfo>> {
        private final YearPersonMonthInfo   owner;
        private       boolean               hasBudget;
        private final List<PersonMonthInfo> subs = new ArrayList<>();

        public PersonMonthInfo(YearPersonMonthInfo owner) {
            this.owner = owner;
        }

        public void add(PersonMonthInfo other) {
            other.forEach((person, mm) -> mm.forEach((month, detail) -> add(person, month, detail)));
            subs.add(other);
        }

        public void add(PersonInfo person, int month, DetailInfo detail) {
            if (owner.pgInfo.isTeamMember(person)) {
                Map<Integer, DetailInfo> m2         = computeIfAbsent(person, y -> new HashMap<>());
                DetailInfo               detailInfo = m2.computeIfAbsent(month, m -> new DetailInfo());
                detailInfo.add(detail);
            }
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

        public boolean hasBudget() {
            return hasBudget || (!subs.isEmpty() && subs.stream().allMatch(PersonMonthInfo::hasBudget));
        }
    }
}
