package nl.modelingvalue.timesheets.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.SheetMaker;
import nl.modelingvalue.timesheets.info.PartInfo;
import nl.modelingvalue.timesheets.util.LogAccu;

public class IndexModel extends Model<IndexModel> {
    private final SheetMaker sheetMaker;

    public IndexModel(SheetMaker sheetMaker) {
        super(null);
        this.sheetMaker = sheetMaker;
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
                    .map(e -> new IndexPageModel(sheetMaker.parts.get(e.getKey()), e.getKey(), e.getValue().stream().map(a -> a[0]).sorted(Comparator.reverseOrder()).toList()))
                    .sorted()
                    .toList();

        } catch (IOException e) {
            throw new Error("unepected error during listing of dir " + dir);
        }
    }

    public boolean traceToHtml() {
        return Config.TRACE_TO_HTML;
    }

    public List<String> getErr() {
        return LogAccu.INSTANCE.err;
    }

    public List<String> getInfo() {
        return LogAccu.INSTANCE.info;
    }

    public List<String> getTrace() {
        return LogAccu.INSTANCE.trace;
    }

    public List<String> getDebug() {
        return LogAccu.INSTANCE.debug;
    }

    public String href(String name, String year) {
        return String.format(Config.TIME_SHEET_FILENAME_TEMPLATE, Integer.parseInt(year), name);
    }

    public static class IndexPageModel implements Comparable<IndexPageModel> {
        private final String       name;
        private final int          index;
        private final List<String> years;

        public IndexPageModel(PartInfo partInfo, String name, List<String> years) {
            this.name  = name;
            this.index = partInfo == null ? Integer.MAX_VALUE : partInfo.index;
            this.years = years;
        }

        public String getName() {
            return name;
        }

        public List<String> getYears() {
            return years;
        }

        @Override
        public int compareTo(IndexPageModel o) {
            return index == o.index ? String.CASE_INSENSITIVE_ORDER.compare(name, o.name) : Integer.compare(index, o.index);
        }
    }
}
