package nl.modelingvalue.timesheets.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.util.LogAccu;

public class IndexModel extends Model<IndexModel> {
    public IndexModel() {
        super(null);
    }

    public List<String> getInfo() {
        return LogAccu.INSTANCE.info;
    }

    public List<String> getErr() {
        return LogAccu.INSTANCE.err;
    }

    public List<String> getLog() {
        return LogAccu.INSTANCE.log;
    }

    public List<IndexPageModel> getPages() {
        Path dir = Paths.get(Config.PUBLIC_DIRNAME);
        try {
            return Files.list(dir)
                    .map(p -> p.getFileName().toString())
                    .filter(s -> s.matches("timesheet-20[0-9][0-9]-[^.]*[.]html"))
                    .map(s -> s.replaceFirst("timesheet-", "").replaceFirst("[.]html$", ""))
                    .map(s -> s.split("-", 2))
                    .collect(Collectors.groupingBy(a1 -> a1[1]))
                    .entrySet()
                    .stream()
                    .map(e -> new IndexPageModel(e.getKey(), e.getValue().stream().map(a -> a[0]).toList()))
                    .toList();

        } catch (IOException e) {
            throw new Error("unepected error during listing of dir " + dir);
        }
    }

    public String href(String name, String year) {
        return String.format(Config.TIME_SHEET_FILENAME_TEMPLATE, Integer.parseInt(year), name);
    }

    public static class IndexPageModel {
        private final String       name;
        private final List<String> years;

        public IndexPageModel(String name, List<String> years) {
            this.name  = name;
            this.years = years;
        }

        public String getName() {
            return name;
        }

        public List<String> getYears() {
            return years;
        }
    }
}
