package nl.modelingvalue.timesheets.model;

import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;

import nl.modelingvalue.timesheets.info.DetailInfo;
import nl.modelingvalue.timesheets.info.PersonInfo;
import nl.modelingvalue.timesheets.util.U;

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
        return U.hoursFromSecFormatted(getSec(DetailInfo::secWorked));
    }

    public String getBudget() {
        return U.hoursFromSecFormatted(getSec(DetailInfo::secBudget));
    }

    public String getBudgetLeft() {
        return U.hoursFromSecFormatted(getBudgetLeftSec());
    }

    public String getBudgetLeftClass() {
        return getBudgetLeftSec() < 0 ? "negative" : "";
    }

    public long getSec(ToLongFunction<DetailInfo> f) {
        return parentModel.projectInfos
                .stream()
                .mapToLong(pi -> pi.accountYearMonthInfo.secFor(personInfo, parentModel.parentModel.year, f))
                .sum();
    }

    public long getBudgetLeftSec() {
        return getSec(DetailInfo::secBudget) - getSec(DetailInfo::secWorked);
    }
}
