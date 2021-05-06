package nl.modelingvalue.timesheets.model;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("unused")
public class MonthModel extends Model<UserModel> {
    public final int month;

    public MonthModel(UserModel userModel, int month) {
        super(userModel);
        this.month = month;
    }

    public long getWorkedSec() {
        return parentModel.parentModel.projectInfos.stream().mapToLong(pi -> pi.accountYearMonthInfo.workSecFor(parentModel.parentModel.parentModel.year, month)).sum();
    }

    public long getBudgetSec() {
        return BUDGET_PLACEHOLDER;
    }

    public String getWorked() {
        return hoursFromSec(getWorkedSec());
    }

    public String getBudget() {
        return hoursFromSec(getBudgetSec());
    }

    public String getUrl() {
        LocalDate firstDay = LocalDate.of(parentModel.parentModel.parentModel.year, month, 1);
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
