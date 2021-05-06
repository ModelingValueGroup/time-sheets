package nl.modelingvalue.timesheets.model;

import java.util.List;
import java.util.stream.IntStream;

import nl.modelingvalue.timesheets.info.PersonInfo;

@SuppressWarnings("unused")
public class UserModel extends Model<TableModel> {
    public final PersonInfo personInfo;

    public UserModel(TableModel tableModel, PersonInfo personInfo) {
        super(tableModel);
        this.personInfo = personInfo;
    }

    public String getName() {
        return personInfo.fullName;
    }

    public List<MonthModel> getMonths() {
        return IntStream.rangeClosed(1, 12).mapToObj(m -> new MonthModel(this, m)).toList();
    }

    public String getWorked() {
        return hoursFromSec(getWorkedSec());
    }

    public String getBudget() {
        return hoursFromSec(getBudgetSec());
    }

    public String getBudgetLeft() {
        return hoursFromSec(getBudgetLeftSec());
    }

    public String getBudgetLeftClass() {
        return getBudgetLeftSec() < 0 ? "negative" : "";
    }

    public long getWorkedSec() {
        return parentModel.projectInfos
                .stream()
                .mapToLong(pi -> pi.accountYearMonthInfo.workSecFor(personInfo, parentModel.parentModel.year))
                .sum();
    }

    public long getBudgetSec() {
        return BUDGET_PLACEHOLDER;
    }

    public long getBudgetLeftSec() {
        return getBudgetSec() - getWorkedSec();
    }
}
