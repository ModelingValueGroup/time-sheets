package nl.modelingvalue.timesheets.model;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;
import java.util.stream.*;

import nl.modelingvalue.timesheets.info.*;
import nl.modelingvalue.timesheets.util.*;

@SuppressWarnings("unused")
public class MonthModel extends Model<UserModel> {
    public final PersonInfo person;
    public final int        year;
    public final int        month;

    public MonthModel(UserModel userModel, int month) {
        super(userModel);
        this.person = userModel.personInfo;
        this.year   = userModel.parentModel.parentModel.year;
        this.month  = month;
    }

    public boolean hasBudget() {
        return getSec(DetailInfo::secBudget) != 0;
    }

    private long getSec(ToLongFunction<DetailInfo> f) {
        return parentModel.parentModel.pgInfo.yearPersonMonthInfo.secFor(person, year, month, f);
    }

    public String getWorked() {
        return hoursFromSecFormatted(getSec(DetailInfo::secWorked));
    }

    public String getBudget() {
        return hoursFromSecFormatted(getSec(DetailInfo::secBudget));
    }

    public String getUrl() {
        return parentModel.parentModel.pgInfo.serverInfoForAllProjects()
                .map(serverInfo -> {
                    LocalDate    firstDay = LocalDate.of(year, month, 1);
                    LocalDate    lastDay  = firstDay.plusMonths(1).minusDays(1);
                    String       url      = serverInfo.url;
                    List<String> projects = parentModel.parentModel.pgInfo.allProjectInfosDeep().map(pi -> pi.projectBean.getKey()).sorted().distinct().collect(Collectors.toList());

                    if (url.contains(".atlassian.net")) {
                        // https://xxxx.atlassian.net/plugins/servlet/ac/jira-timesheet-plugin/timereports-report#!
                        //      project.key         =   ACDS&
                        //      user                =   nnnnnnnnnn&
                        //      startDate           =   2021-10-02&
                        //      endDate             =   2021-10-31&
                        //      showDetails         =   true&
                        //      view                =   week&
                        //      sum                 =   day
                        String user      = person.accountIdMap.get(serverInfo);
                        String startDate = firstDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String endDate   = lastDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        if (user == null) {
                            LogAccu.err("can not make link to timesheet because no account id (user=" + person.fullName + " projects=" + projects + " period=[" + startDate + "..." + endDate + "]");
                        }

                        UrlBuilder b = new UrlBuilder(url + "/plugins/servlet/ac/jira-timesheet-plugin/timereports-report#!");
                        projects.forEach(key -> b.append("project.key", key));
                        b.append("user", user);
                        b.append("startDate", startDate);
                        b.append("endDate", endDate);
                        b.append("showDetails", "true");
                        b.append("view", "week");
                        b.append("sum", "day");
                        return b.toString();
                    } else {
                        // https://xxx.other-server.com/secure/ConfigureReport.jspa?
                        //      reportKey           =   jira-timesheet-plugin%3Areport&
                        //      startDate           =   01%2FNov%2F20&
                        //      endDate             =   30%2FNov%2F20&
                        //      targetUser          =   dirk+scheele&
                        //      projectid           =   10005&
                        //      targetGroup         =   &
                        //      excludeTargetGroup  =   &
                        //      projectRoleId       =   &
                        //      filterid            =   &
                        //      priority            =   &
                        //      commentfirstword    =   &
                        //      weekends            =   true&
                        //      sum                 =   day&
                        //      groupByField        =   &
                        //      moreFields          =   &
                        //      sortBy              =   &
                        //      sortDir             =   ASC&
                        //      Next                =   Next
                        String user      = parentModel.getName().toLowerCase();
                        String startDate = firstDay.format(DateTimeFormatter.ofPattern("dd/MMM/yy"));
                        String endDate   = lastDay.format(DateTimeFormatter.ofPattern("dd/MMM/yy"));

                        UrlBuilder b = new UrlBuilder(url + "/secure/ConfigureReport.jspa");
                        b.append("reportKey", "jira-timesheet-plugin:report");
                        b.append("startDate", startDate);
                        b.append("endDate", endDate);
                        b.append("targetUser", user);
                        projects.forEach(id -> b.append("projectid", id));
                        b.append("targetGroup", "");
                        b.append("excludeTargetGroup", "");
                        b.append("projectRoleId", "");
                        b.append("filterid", "");
                        b.append("priority", "");
                        b.append("commentfirstword", "");
                        b.append("weekends", "true");
                        b.append("sum", "day");
                        b.append("groupByField", "");
                        b.append("moreFields", "");
                        b.append("sortBy", "");
                        b.append("sortDir", "ASC");
                        b.append("Next", "Next");
                        return b.toString();
                    }
                })
                .orElse(null);
    }

    private List<ProjectInfo> allProjectsOfMostFrequentJiraServer() {
        return getMostFrequentJiraServerEntry()
                .map(Entry::getValue)
                .orElse(Collections.emptyList());
    }

    private ServerInfo getMostFrequentJiraServer() {
        return getMostFrequentJiraServerEntry()
                .map(Entry::getKey)
                .orElse(null);
    }

    private Optional<Entry<ServerInfo, List<ProjectInfo>>> getMostFrequentJiraServerEntry() {
        return parentModel.parentModel.pgInfo.allProjectInfosDeep()
                .filter(pi -> pi.serverInfo != null)
                .collect(Collectors.groupingBy(pi -> pi.serverInfo))
                .entrySet()
                .stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()));
    }

    public String getDetails() {
        Map<String, Long> all = parentModel.parentModel.pgInfo.yearPersonMonthInfo.allSecFor(person, year, month, DetailInfo::secWorked);
        if (all == null) {
            return null;
        }
        List<Entry<String, Long>> entries = all
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != 0)
                .sorted(Entry.comparingByKey())
                .toList();
        if (parentModel.parentModel.pgInfo.yearPersonMonthInfo.hasBudget(year)) {
            long budgetSec = parentModel.parentModel.pgInfo.yearPersonMonthInfo.secFor(person, year, month, DetailInfo::secBudget);
            if (0 < budgetSec) {
                entries = new ArrayList<>(entries);
                entries.add(new AbstractMap.SimpleEntry<>("budget", budgetSec));
            }
        }
        if (entries.stream().mapToLong(Entry::getValue).sum() == 0) {
            return null;
        }
        return entries
                .stream()
                .map(e -> e.getKey() + ": " + hoursFromSecFormatted(e.getValue()))
                .collect(Collectors.joining("<br>"));
    }
}
