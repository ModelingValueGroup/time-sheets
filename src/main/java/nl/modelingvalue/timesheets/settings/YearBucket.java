package nl.modelingvalue.timesheets.settings;

import java.util.HashMap;

public class YearBucket extends HashMap<String, ProjectBucket> {
    public int year;

    public void init(String year) {
        this.year = Integer.parseInt(year);
        forEach((n, p) -> p.init(this.year, n));
    }
}
