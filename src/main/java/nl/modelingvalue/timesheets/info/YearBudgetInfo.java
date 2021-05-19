package nl.modelingvalue.timesheets.info;

import java.util.HashMap;

import nl.modelingvalue.timesheets.SheetMaker;
import nl.modelingvalue.timesheets.util.FatalException;

public class YearBudgetInfo extends HashMap<String, PersonBudgetInfo> {
    public  String     id;
    public  int        index;
    public  int        year;
    //
    private SheetMaker sheetMaker;

    public void init(SheetMaker sheetMaker) {
        this.sheetMaker = sheetMaker;
        year            = parseId();

        values().forEach(v -> v.init(sheetMaker));
    }

    private int parseId() {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new FatalException("the id of budget entry '" + id + "' should be an integer");
        }
    }
}
