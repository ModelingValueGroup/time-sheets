package nl.modelingvalue.timesheets.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import nl.modelingvalue.timesheets.info.DetailInfo;
import nl.modelingvalue.timesheets.info.PersonInfo;
import nl.modelingvalue.timesheets.info.ProjectInfo;
import nl.modelingvalue.timesheets.util.UrlBuilder;

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
        return parentModel.parentModel.pgInfo.serverUrlForAllProjects()
                .map(url -> {
                    LocalDate firstDay  = LocalDate.of(year, month, 1);
                    LocalDate lastDay   = firstDay.plusMonths(1).minusDays(1);
                    String    user      = parentModel.getName().toLowerCase();
                    String    startDate = firstDay.format(DateTimeFormatter.ofPattern("dd/MMM/yy"));  // should be dd/MMM/yy
                    String    endDate   = lastDay.format(DateTimeFormatter.ofPattern("dd/MMM/yy"));


                    if (url.contains(".atlassian.net/")) {
                        // https://xxxx.atlassian.net/plugins/servlet/ac/jira-timesheet-plugin/timereports-report#!
                        //      project.key         =   ACDS&
                        //      user                =   6001540f1051d10075f20b72&
                        //      startDate           =   2021-10-02&
                        //      endDate             =   2021-10-31&
                        //      showDetails         =   true&
                        //      view                =   week&
                        //      sum                 =   day
                        UrlBuilder b = new UrlBuilder(url + "/plugins/servlet/ac/jira-timesheet-plugin/timereports-report#!");
                        parentModel.parentModel.pgInfo.allProjectInfosDeep().map(pi -> pi.projectBean.getKey()).sorted().distinct().forEach(key -> b.append("project.key", key));
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
                        UrlBuilder b = new UrlBuilder(url + "/secure/ConfigureReport.jspa");
                        b.append("reportKey", "jira-timesheet-plugin:report");
                        b.append("startDate", startDate);
                        b.append("endDate", endDate);
                        b.append("targetUser", user);
                        parentModel.parentModel.pgInfo.allProjectInfosDeep().map(pi -> pi.projectBean.getId()).sorted().distinct().forEach(id -> b.append("projectid", id));
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
        return parentModel.parentModel.pgInfo.allProjectInfosDeep()
                .filter(pi -> pi.serverInfo != null)
                .collect(Collectors.groupingBy(pi -> pi.serverInfo))
                .entrySet()
                .stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Entry::getValue)
                .orElse(Collections.emptyList());
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
