package nl.modelingvalue.timesheets.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class MonthModel extends Model<UserModel> {
    public static final MonthModel EMPTY = new MonthModel(null, 0);

    public final int            month;
    public final List<WorkInfo> workList = new ArrayList<>();

    public MonthModel(UserModel userModel, WorkInfo wi) {
        this(userModel, wi.month());
    }

    public MonthModel(UserModel userModel, int month) {
        super(userModel);
        this.month = month;
    }

    public void add(WorkInfo wi) {
        synchronized (workList) {
            workList.add(wi);
        }
    }

    <T> Stream<T> selectFromAllWork(Function<WorkInfo, T> selector) {
        return workList.stream().map(selector);
    }

    public long getWorkedSec() {
        return selectFromAllWork(WorkInfo::seconds).mapToLong(l -> l).sum();
    }

    public long getBudgetSec() {
        return BUDGET_PLACEHOLDER;
    }

    public String getWorked() {
        return workHoursFromSec(getWorkedSec());
    }

    public String getBudget() {
        return workHoursFromSec(getBudgetSec());
    }

    public String getUrl() {
        LocalDate firstDay = LocalDate.of(parentModel.parentModel.parentModel.year, month, 1);
        LocalDate lastDay  = firstDay.plusMonths(1).minusDays(1);

        String       root           = parentModel.parentModel.parentModel.repoBucket.url;
        List<String> allProjectKeys = selectFromAllWork(wi -> wi.projectBean().getId()).distinct().toList();
        String       projectId      = allProjectKeys.size() == 1 ? allProjectKeys.get(0) : allProjectKeys.toString();
        String       startDate      = firstDay.format(DATE_FORMATTER);
        String       endDate        = lastDay.format(DATE_FORMATTER);
        String       user           = parentModel.name.toLowerCase();
        return root + "/secure/ConfigureReport.jspa?targetGroup=&priority=&filterid=&projectRoleId=&weekends=true&groupByField=&moreFields=&selectedProjectId=&reportKey=jira-timesheet-plugin:report&Next=Next&projectid=" + projectId + "&startDate=" + startDate + "&endDate=" + endDate + "&targetUser=" + user + "&";
    }
}
