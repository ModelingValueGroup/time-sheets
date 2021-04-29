package nl.modelingvalue.timesheets.util;

import java.util.regex.Pattern;

public class U {
    public static Pattern cachePattern(String s) {
        String regexp = "";
        if (s != null) {
            if (s.startsWith("/") && s.endsWith("/") && 2 <= s.length()) {
                regexp = s.substring(1, s.length() - 1);
            } else {
                regexp = "^" + Pattern.quote(s) + "$";
            }
        }
        return Pattern.compile(regexp);
    }
}
