package nl.modelingvalue.timesheets.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class UserModel extends Model<ProjectModel> {
    public final String                   name;
    public final Map<Integer, MonthModel> monthMap = new HashMap<>();

    public UserModel(ProjectModel projectModel, WorkInfo wi) {
        super(projectModel);
        this.name = wi.userBucketName();
    }

    public  void add(WorkInfo wi) {
        getOrCreateSubModel(monthMap, wi.month(), __ -> new MonthModel(this, wi))
                .add(wi);
    }

    <T> Stream<T> selectFromAllWork(Function<WorkInfo, T> selector) {
        return monthMap.values().stream().flatMap(e -> e.selectFromAllWork(selector));
    }

    public String getName() {
        return name;
    }

    public List<MonthModel> getMonths() {
        return IntStream.rangeClosed(1, 12).mapToObj(n -> monthMap.getOrDefault(n, new MonthModel(this, n))).toList();
    }

    public String getWorked() {
        return workHoursFromSec(getWorkedSec());
    }

    public String getBudget() {
        return workHoursFromSec(getBudgetSec());
    }

    public String getBudgetLeft() {
        return workHoursFromSec(getBudgetLeftSec());
    }

    public String getBudgetLeftClass() {
        return getBudgetLeftSec() < 0 ? "negative" : "";
    }

    public long getWorkedSec() {
        return selectFromAllWork(WorkInfo::seconds).mapToLong(s -> s).sum();
    }

    public long getBudgetSec() {
        return BUDGET_PLACEHOLDER;
    }

    public long getBudgetLeftSec() {
        return getBudgetSec() - getWorkedSec();
    }
}
