package nl.modelingvalue.timesheets.util;

import java.util.List;

public class Jql {
    public static String and(String left, String right) {
        return left + " AND " + right;
    }

    public static String in(String fieldName, List<String> projectKeys) {
        return fieldName + " in (" + String.join(", ", projectKeys.stream().map(s -> "\"" + s + "\"").toList()) + ")";
    }
}
