package nl.modelingvalue.timesheets.model;

import static nl.modelingvalue.timesheets.util.Jql.DATE_FORMATTER;

import java.time.LocalDate;
import java.util.List;
import java.util.function.ToLongFunction;

import nl.modelingvalue.timesheets.info.DetailInfo;
import nl.modelingvalue.timesheets.info.PersonInfo;
import nl.modelingvalue.timesheets.util.U;

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
        return parentModel.parentModel.projectInfos.stream().mapToLong(pi -> pi.accountYearMonthInfo.secFor(person, year, month, f)).sum();
    }

    public String getWorked() {
        return U.hoursFromSecFormatted(getSec(DetailInfo::secWorked));
    }

    public String getBudget() {
        return U.hoursFromSecFormatted(getSec(DetailInfo::secBudget));
    }

    public String getUrl() {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay  = firstDay.plusMonths(1).minusDays(1);

        String       root           = parentModel.parentModel.projectInfos.stream().filter(pi -> pi.serverInfo != null).findFirst().orElseThrow().serverInfo.url;
        List<String> allProjectKeys = parentModel.parentModel.projectInfos.stream().filter(pi -> pi.getProjectBean() != null).map(pi -> pi.getProjectBean().getKey()).toList();
        String       projectId      = allProjectKeys.size() == 1 ? allProjectKeys.get(0) : allProjectKeys.toString();
        String       startDate      = firstDay.format(DATE_FORMATTER);
        String       endDate        = lastDay.format(DATE_FORMATTER);
        String       user           = parentModel.getName().toLowerCase();
        return root + "/secure/ConfigureReport.jspa?targetGroup=&priority=&filterid=&projectRoleId=&weekends=true&groupByField=&moreFields=&selectedProjectId=&reportKey=jira-timesheet-plugin:report&Next=Next&projectid=" + projectId + "&startDate=" + startDate + "&endDate=" + endDate + "&targetUser=" + user + "&";
    }
}
