package nl.modelingvalue.timesheets.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.SheetMaker;

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

    public Optional<String> serverUrlForAllProjects() {
        Set<String> urls = allProjectInfosDeep().filter(pi -> pi.serverInfo != null).map(pi -> pi.serverInfo.url).collect(Collectors.toSet());
        return urls.size() != 1 ? Optional.empty() : Optional.of(urls.iterator().next());
    }
}
