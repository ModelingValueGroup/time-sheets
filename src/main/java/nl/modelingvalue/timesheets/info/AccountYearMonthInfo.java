package nl.modelingvalue.timesheets.info;

import static java.util.Collections.emptyMap;
import static nl.modelingvalue.timesheets.info.DetailInfo.EMPTY_DETAIL;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.util.U;

public class AccountYearMonthInfo extends Info {
    private final Map<PersonInfo, Map<Integer, Map<Integer, DetailInfo>>> map = new HashMap<>();

    public AccountYearMonthInfo() {
    }

    public void add(Collection<YearBudgetInfo> budgets) {
        budgets
                .forEach(ybi -> ybi.values()
                        .forEach(pbi -> IntStream.rangeClosed(1, 12)
                                .forEach(month -> add(pbi.personInfo, ybi.year, month, new DetailInfo(0, U.secFromHours(pbi.months[month - 1]))))));
    }

    public synchronized void add(AccountYearMonthInfo other) {
        other.map.forEach((person, ym) -> ym.forEach((year, mm) -> mm.forEach((month, detail) -> add(person, year, month, detail))));
    }

    public synchronized void add(PersonInfo person, int year, int month, DetailInfo detail) {
        Map<Integer, Map<Integer, DetailInfo>> m1         = map.computeIfAbsent(person, ab -> new HashMap<>());
        Map<Integer, DetailInfo>               m2         = m1.computeIfAbsent(year, y -> new HashMap<>());
        DetailInfo                             detailInfo = m2.computeIfAbsent(month, m -> new DetailInfo());
        detailInfo.add(detail);
    }

    public Stream<Integer> getYears() {
        return map.values().stream().flatMap(m -> m.keySet().stream()).distinct();
    }

    public List<PersonInfo> getPersonInfos(int year) {
        return map.keySet().stream().filter(pi -> !map.get(pi).getOrDefault(year, Collections.emptyMap()).isEmpty()).toList();
    }

    public long secFor(int year, ToLongFunction<DetailInfo> f) {
        return map.values()
                .stream()
                .flatMapToLong(
                        y -> y.getOrDefault(year, emptyMap())
                                .values()
                                .stream()
                                .mapToLong(f)
                ).sum();
    }

    public long secFor(int year, int month, ToLongFunction<DetailInfo> f) {
        return map.values()
                .stream()
                .mapToLong(
                        y -> f.applyAsLong(y.getOrDefault(year, emptyMap()).getOrDefault(month, EMPTY_DETAIL))
                ).sum();
    }

    public long secFor(PersonInfo personInfo, int year, ToLongFunction<DetailInfo> f) {
        return map.getOrDefault(personInfo, emptyMap())
                .getOrDefault(year, emptyMap())
                .values()
                .stream()
                .mapToLong(f)
                .sum();
    }

    public long secFor(PersonInfo personInfo, int year, int month, ToLongFunction<DetailInfo> f) {
        return f.applyAsLong(map.getOrDefault(personInfo, emptyMap())
                .getOrDefault(year, emptyMap())
                .getOrDefault(month, EMPTY_DETAIL));
    }

    public boolean notEmpty(int year) {
        return secFor(year, d -> d.secWorked() + d.secBudget()) != 0;
    }
}
