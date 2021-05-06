package nl.modelingvalue.timesheets.util;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public class U {
    public static Pattern cachePattern(String s) {
        String regexp = "/.*/";
        if (s != null) {
            if (s.startsWith("/") && s.endsWith("/") && 2 <= s.length()) {
                regexp = s.substring(1, s.length() - 1);
            } else {
                regexp = "^" + Pattern.quote(s) + "$";
            }
        }
        return Pattern.compile(regexp, CASE_INSENSITIVE);
    }

    static String readResource(String name) {
        try {
            URL resource = PageEncryptWrapper.class.getResource(name);
            if (resource == null) {
                throw new Error("wrapper source not found");
            }
            return Files.readString(Paths.get(resource.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new Error("could not read wrapper source from resource", e);
        }
    }

    public static <T> T errorIfNull(T o, String role, String name) {
        if (o == null) {
            throw new Error("can not find " + role + " with name " + name);
        }
        return o;
    }
}
