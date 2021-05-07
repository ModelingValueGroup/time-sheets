package nl.modelingvalue.timesheets;

import java.util.List;
import java.util.regex.Pattern;

public class Config {
    public static final boolean      CURRENT_YEAR_ONLY            = Boolean.getBoolean("CURRENT_YEAR_ONLY");
    //
    public static final String       TIME_SHEET_FILENAME_TEMPLATE = System.getProperty("TIME_SHEET_FILENAME_TEMPLATE", "timesheet-%4d-%s.html");
    public static final String       RAW_DIRNAME                  = System.getProperty("RAW_DIRNAME", "raws");
    public static final String       PUBLIC_DIRNAME               = System.getProperty("PUBLIC_DIRNAME", "docs");
    public static final Pattern      DEFAULT_NAME_PAT             = Pattern.compile("^time-sheets-.*.json$");
    public static final List<String> DEFAULT_DIRS                 = List.of(".", "..", System.getProperty("user.home"));
    //
    public static final String       NOT_YET_IMPLEMENTED_URL      = "http://www.bamu-gereedschappen.nl/wp-content/uploads/2018/09/under-construction.jpg";
    public static final String[]     MONTH_NAMES                  = {
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"
    };
}
