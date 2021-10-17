package nl.modelingvalue.timesheets.model;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import nl.modelingvalue.timesheets.info.*;
import nl.modelingvalue.timesheets.util.*;

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
        return hoursFromSecFormatted(getSec(DetailInfo::secWorked));
    }

    public String getBudget() {
        return parentModel.hasBudget() ? hoursFromSecFormatted(getSec(DetailInfo::secBudget)) : "";
    }

    public String getBudgetLeft() {
        return parentModel.hasBudget() ? hoursFromSecFormatted(getBudgetLeftSec()) : "";
    }

    public String getBudgetLeftClass() {
        return U.jsClasses(getBudgetLeftSec(), "budgetLeft", "budget");
    }

    public long getSec(ToLongFunction<DetailInfo> f) {
        return parentModel.pgInfo.yearPersonMonthInfo.secFor(personInfo, parentModel.parentModel.year, f);
    }

    public long getBudgetLeftSec() {
        return getSec(DetailInfo::secBudget) - getSec(DetailInfo::secWorked);
    }
}
