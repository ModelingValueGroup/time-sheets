package nl.modelingvalue.timesheets.info;

import java.util.*;
import java.util.stream.*;

import nl.modelingvalue.timesheets.*;

@SuppressWarnings("FieldMayBeFinal")
public abstract class PGInfo extends Info {
    public String                      team;
    public Map<String, YearBudgetInfo> budgets             = new HashMap<>();
    //
    public YearPersonMonthInfo         yearPersonMonthInfo = new YearPersonMonthInfo(this);

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        budgets.values().forEach(v -> v.init(sheetMaker));
        yearPersonMonthInfo.init(sheetMaker);
        yearPersonMonthInfo.add(budgets.values());
    }

    public TeamInfo getTeamInfo() {
        return team == null ? null : sheetMaker.resolveTeam(team);
    }

    public Stream<Integer> getYears() {
        return yearPersonMonthInfo.getYears();
    }

    public abstract Stream<ProjectInfo> allProjectInfosDeep();

    public abstract Stream<PGInfo> allSubInfos();

    public abstract void accumulateSubs();

    public boolean notEmpty(int year) {
        return yearPersonMonthInfo.notEmpty(year);
    }

    public boolean isTeamMember(PersonInfo person) {
        return team == null || getTeamInfo().isMember(person);
    }

    public Stream<PersonInfo> getTeamStream() {
        return team == null ? Stream.empty() : getTeamInfo().getTeamStream();
    }

    public Optional<ServerInfo> serverInfoForAllProjects() {
        Set<ServerInfo> urls = allProjectInfosDeep().map(pi -> pi.serverInfo).filter(Objects::nonNull).collect(Collectors.toSet());
        return urls.size() != 1 ? Optional.empty() : Optional.of(urls.iterator().next());
    }
}
