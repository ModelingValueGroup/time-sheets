package nl.modelingvalue.timesheets.settings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import nl.modelingvalue.timesheets.util.GsonUtils;

public class Settings {
    public static final String REPOS_CMD_LINE_FLAG    = "-r";
    public static final Path   REPOS_TIME_SHEETS_JSON = Paths.get(".time-sheets-repos.json");
    public static final Type   REPOS_BUCKET_MAP_TYPE  = new TypeToken<Map<String, RepoBucket>>() {
    }.getType();
    public static final String YEARS_CMD_LINE_FLAG    = "-y";
    public static final Path   YEARS_TIME_SHEETS_JSON = Paths.get(".time-sheets-years.json");
    public static final Type   YEARS_BUCKET_MAP_TYPE  = new TypeToken<Map<String, YearBucket>>() {
    }.getType();

    public Map<String, RepoBucket> reposMap;
    public Map<String, YearBucket> yearsMap;

    public Settings(Map<String, RepoBucket> reposMap, Map<String, YearBucket> yearsMap) {
        this.reposMap = reposMap;
        this.yearsMap = yearsMap;
        reposMap.forEach((n, r) -> r.init(n));
        yearsMap.forEach((n, y) -> y.init(n));
    }

    public static Settings read(String[] args) {
        Path reposFile = getFile(args, REPOS_TIME_SHEETS_JSON, REPOS_CMD_LINE_FLAG);
        Path yearsFile = getFile(args, YEARS_TIME_SHEETS_JSON, YEARS_CMD_LINE_FLAG);

        Map<String, RepoBucket> repos = read(reposFile, REPOS_BUCKET_MAP_TYPE);
        Map<String, YearBucket> years = read(yearsFile, YEARS_BUCKET_MAP_TYPE);

        System.err.println("settings read from:");
        System.err.println("    repos map: " + reposFile.toAbsolutePath() + " (with " + repos.size() + " repos)");
        System.err.println("    years map: " + yearsFile.toAbsolutePath() + " (with " + years.size() + " years)");

        return new Settings(repos, years);
    }

    private static <T> Map<String, T> read(Path f, Type type) {
        try {
            return GsonUtils.withoutRecords().fromJson(new JsonReader(Files.newBufferedReader(f)), type);
        } catch (IOException e) {
            throw new Error("can not read " + f.toAbsolutePath(), e);
        }
    }

    private static Path getFile(String[] args, Path defaultFile, String flag) {
        Optional<Path> found = getFrom(args, flag);
        if (found.isEmpty()) {
            found = getFrom(defaultFile, ".");
            if (found.isEmpty()) {
                found = getFrom(defaultFile, "..");
                if (found.isEmpty()) {
                    found = getFrom(defaultFile, System.getProperty("user.home"));
                }
            }
        }
        return found.orElseThrow(() -> {
            throw new Error("time-sheet settings file not found: " + defaultFile);
        });
    }

    private static Optional<Path> getFrom(String[] args, String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(flag) && (i + 1) < args.length) {
                Path f = Paths.get(args[i + 1]);
                if (Files.isRegularFile(f)) {
                    return Optional.of(f);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Path> getFrom(Path defaultFile, String dir) {
        if (dir == null || dir.isBlank()) {
            return Optional.empty();
        } else {
            Path f = Paths.get(dir).resolve(defaultFile);
            return Files.isRegularFile(f) ? Optional.of(f) : Optional.empty();
        }
    }
}
