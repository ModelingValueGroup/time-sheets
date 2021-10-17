package nl.modelingvalue.timesheets.info;

import java.util.stream.*;

import nl.modelingvalue.timesheets.*;

public class PersonBudgetInfo extends Info {
    public double     allMonths;
    public double[]   months;
    //
    public PersonInfo personInfo;

    public PersonBudgetInfo() {
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        if (months == null) {
            months = IntStream.range(0, 12).mapToDouble(i -> allMonths).toArray();
        }
        personInfo = sheetMaker.resolvePerson(id);
    }
}
