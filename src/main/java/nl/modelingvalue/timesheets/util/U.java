package nl.modelingvalue.timesheets.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import java.util.zip.*;

import de.micromata.jira.rest.core.util.*;

import static java.util.regex.Pattern.*;
import static nl.modelingvalue.timesheets.util.LogAccu.*;

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
                throw new FatalException("resource '" + name + "' not found");
            }
            return readInputStreamAsString(stream);
        } catch (IOException e) {
            throw new Wrapper("could not read resource '" + name + "'", e);
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

    public static Stream<Path> selectJsonFiles(Path fd, Pattern defaultNamePat) {
        try {
            if (Files.isRegularFile(fd) && fd.getFileName().toString().endsWith(".json")) {
                return Stream.of(fd);
            }
            if (Files.isDirectory(fd)) {
                return Files.list(fd).filter(f -> defaultNamePat.matcher(f.getFileName().toString()).matches());
            }
            info("not a file or dir: " + fd.toAbsolutePath());
        } catch (IOException e) {
            err("dir " + fd.toAbsolutePath() + " can not be scanned for sheetMaker files (" + e + ")");
        }
        return Stream.empty();
    }

    public static long secFromHours(double hours) {
        long quarters = Math.round(hours * 4.0);
        return quarters * (60 * 60 / 4);
    }

    public static double hoursFromSec(long sec) {
        double quarters = Math.round(sec * (4.0 / (60.0 * 60.0)));
        return quarters / 4.0;
    }

    public static long copyResourceCrc(Path file) {
        return copyResourceCrc(file, file.getFileName().toString(), s -> s);
    }

    public static long copyResourceCrc(Path file, String rsrcName, Function<String, String> filter) {
        return writeStringCrc(file, filter.apply(readResource(rsrcName)));
    }

    public static long writeStringCrc(Path file, String filtered) {
        try {
            Files.writeString(file, filtered);
            return crc(filtered);
        } catch (IOException e) {
            throw new Wrapper(e);
        }
    }

    private static long crc(String filtered) {
        Checksum crc32 = new CRC32();
        crc32.update(filtered.getBytes());
        return crc32.getValue();
    }

    public static String jsClasses(long sec, String... otherclasses) {
        Stream<String> stream = Arrays.stream(otherclasses);
        if (sec < 0) {
            stream = Stream.concat(stream, Stream.of("negative"));
        }
        return stream.collect(Collectors.joining(" "));
    }

    public static void createDirectories(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new Wrapper(e);
        }
    }

    public static String makeCrcJson(long crc) {
        return String.format("{\"crc\":\"0x%08x\"}", crc);
    }
}
