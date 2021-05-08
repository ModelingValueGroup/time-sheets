package nl.modelingvalue.timesheets.util;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
            InputStream stream = PageEncryptWrapper.class.getResourceAsStream(name);
            if (stream == null) {
                throw new Error("wrapper source not found");
            }
            return readInputStreamAsString(stream);
        } catch (IOException e) {
            throw new Error("could not read wrapper source from resource", e);
        }
    }

    public static String readInputStreamAsString(InputStream in) throws IOException {
        BufferedInputStream   bis    = new BufferedInputStream(in);
        ByteArrayOutputStream buf    = new ByteArrayOutputStream();
        int                   result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    public static <T> T errorIfNull(T o, String role, String name) {
        if (o == null) {
            throw new Error("can not find " + role + " with name " + name);
        }
        return o;
    }

    public static Stream<Path> selectJsonFiles(Path fd, Pattern defaultNamePat) {
        try {
            if (Files.isRegularFile(fd) && fd.getFileName().toString().endsWith(".json")) {
                return Stream.of(fd);
            }
            if (Files.isDirectory(fd)) {
                return Files.list(fd).filter(f -> defaultNamePat.matcher(f.getFileName().toString()).matches());
            }
            LogAccu.info("not a file or dir: " + fd.toAbsolutePath());
        } catch (IOException e) {
            LogAccu.info("dir " + fd.toAbsolutePath() + " can not be scanned for sheetMaker files (" + e + ")");
        }
        return Stream.empty();
    }

    public static long secFromHours(double hours) {
        long quarters = Math.round(hours * 4.0);
        return quarters * (60 * 60 / 4);
    }

    public static double hoursFromSec(long sec) {
        double dsec     = sec;
        double quarters = Math.round(dsec * (4.0 / (60.0 * 60.0)));
        return quarters / 4.0;
    }

    public static String hoursFromSecFormatted(long totalSec) {
        return String.format("%4.2f", hoursFromSec(totalSec));
    }

}
