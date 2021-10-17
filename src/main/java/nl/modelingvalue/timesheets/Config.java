package nl.modelingvalue.timesheets;

import java.util.*;
import java.util.regex.*;

public class Config {
    public static final boolean      TRACE_TO_STDERR              = Boolean.getBoolean("TRACE_TO_STDERR");
    public static final boolean      TRACE_TO_HTML                = Boolean.getBoolean("TRACE_TO_HTML");
    public static final boolean      CURRENT_YEAR_ONLY            = Boolean.getBoolean("CURRENT_YEAR_ONLY");
    //
    public static final String       TIME_SHEET_FILENAME_TEMPLATE = System.getProperty("TIME_SHEET_FILENAME_TEMPLATE", "timesheet-%4d-%s.html");
    public static final String       INDEX_FILENAME               = "index.html";
    public static final String       PAGE_HTML_TEMPLATE           = "page.html";
    public static final String       INDEX_HTML_TEMPLATE          = "index.html";
    public static final String       RAW_DIRNAME                  = System.getProperty("RAW_DIRNAME", "raws");
    public static final String       PUBLIC_DIRNAME               = System.getProperty("PUBLIC_DIRNAME", "docs");
    public static final Pattern      DEFAULT_NAME_PAT             = Pattern.compile("^time-sheets-.*.json$");
    public static final List<String> DEFAULT_DIRS                 = List.of(".", "..", System.getProperty("user.home"));
    //
    public static final String       STYLES_CSS                   = "styles.css";
    public static final String       SCRIPTS_JS                   = "scripts.js";
    public static final List<String> SUPPORT_FILES                = List.of(STYLES_CSS, SCRIPTS_JS);
    public static final String[]     MONTH_NAMES                  = {
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"
    };
}
