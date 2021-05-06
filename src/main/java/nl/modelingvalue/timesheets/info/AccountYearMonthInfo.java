package nl.modelingvalue.timesheets.info;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.micromata.jira.rest.core.domain.AccountBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;

public class AccountYearMonthInfo extends Info {
    public static final Map<Integer, DetailInfo>                                 EMPTY_MAP   = Collections.emptyMap();
    public static final List<AccountBean>                                        EMPTY_LIST  = Collections.emptyList();
    //
    private final       Map<AccountBean, Map<Integer, Map<Integer, DetailInfo>>> map         = new HashMap<>();
    private             Map<PersonInfo, List<AccountBean>>                       personIndex = new HashMap<>();


    public synchronized void add(WorkEntryBean wb) {
        Map<Integer, Map<Integer, DetailInfo>> m1         = map.computeIfAbsent(wb.getAuthor(), ab -> new HashMap<>());
        Map<Integer, DetailInfo>               m2         = m1.computeIfAbsent(wb.getStartedDate().getYear(), y -> new HashMap<>());
        DetailInfo                             detailInfo = m2.computeIfAbsent(wb.getStartedDate().getMonthValue(), m -> new DetailInfo());
        detailInfo.add(wb);
    }

    public void makePersonIndex() {
        personIndex = map.keySet().stream().collect(Collectors.groupingBy(settings::findPerson));
    }

    public Stream<Integer> getYears() {
        return map.values().stream().flatMap(m -> m.keySet().stream()).distinct();
    }

    public List<PersonInfo> getPersonInfos() {
        return personIndex.keySet().stream().toList();
    }

    public long workSecFor(int year) {
        return map.values()
                .stream()
                .flatMapToLong(
                        y -> y.getOrDefault(year, EMPTY_MAP)
                                .values()
                                .stream()
                                .mapToLong(DetailInfo::secWorked)
                ).sum();
    }

    public long workSecFor(int year, int month) {
        return map.values()
                .stream()
                .mapToLong(
                        y -> y.getOrDefault(year, EMPTY_MAP)
                                .getOrDefault(month, DetailInfo.EMPTY)
                                .secWorked()
                ).sum();
    }

    public long workSecFor(PersonInfo personInfo, int year) {
        return personIndex
                .getOrDefault(personInfo, EMPTY_LIST)
                .stream()
                .flatMapToLong(ab -> map.get(ab)
                        .getOrDefault(year, EMPTY_MAP)
                        .values()
                        .stream()
                        .mapToLong(DetailInfo::secWorked))
                .sum();
    }

    public boolean notEmpty(int year) {
        return workSecFor(year) != 0;
    }
}
