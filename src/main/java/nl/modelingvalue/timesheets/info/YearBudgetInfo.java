package nl.modelingvalue.timesheets.info;

import java.util.HashMap;

public class YearBudgetInfo extends HashMap<String, PersonBudgetInfo> {
    public  String   id;
    public  int      index;
    public  int      year;
    //
    private Settings settings;

    public void init(Settings settings) {
        this.settings = settings;
        year          = parseId();

        values().forEach(v -> v.init(settings));
    }

    private int parseId() {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new Error("the id of the budget entry '" + id + "' should be an integer");
        }
    }
}
