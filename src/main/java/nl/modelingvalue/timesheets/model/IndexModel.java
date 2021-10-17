package nl.modelingvalue.timesheets.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import de.micromata.jira.rest.core.util.*;
import nl.modelingvalue.timesheets.*;
import nl.modelingvalue.timesheets.util.*;

public class IndexModel extends Model<IndexModel> {
    private final static Path PUBLIC_DIR = Paths.get(Config.PUBLIC_DIRNAME);

    private final SheetMaker   sheetMaker;
    private final List<String> allYears;

    public IndexModel(SheetMaker sheetMaker) {
        super(null);
        this.sheetMaker = sheetMaker;
        this.allYears   = year_name_StreamFromFiles().map(y_n -> y_n[0]).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    public String getStylesCss() {
        return Config.STYLES_CSS;
    }

    public String getScriptsJs() {
        return Config.SCRIPTS_JS;
    }

    public List<IndexGroupModel> getGroups() {
        return year_name_StreamFromFiles()
                .collect(Collectors.groupingBy(year_name -> year_name[1]))
                .entrySet()
                .stream()
                .filter(e -> sheetMaker.groups.containsKey(e.getKey()))
                .map(e -> {
                    List<String> years = getYears(e.getValue()
                            .stream()
                            .map(year_name -> year_name[0])
                            .collect(Collectors.toSet()));
                    return new IndexGroupModel(e.getKey(), sheetMaker.publish.indexOf(e.getKey()), years);
                })
                .sorted()
                .toList();
    }

    private Stream<String[]> year_name_StreamFromFiles() {
        try {
            return Files.list(PUBLIC_DIR)
                    .map(p -> p.getFileName().toString())
                    .filter(s -> s.matches("timesheet-20[0-9][0-9]-[^.]*[.]html"))
                    .map(s -> s.replaceFirst("timesheet-", "").replaceFirst("[.]html$", ""))
                    .map(s -> s.split("-", 2));
        } catch (IOException e) {
            throw new Wrapper("problem during listing of dir " + PUBLIC_DIR, e);
        }
    }

    private List<String> getYears(Set<String> years) {
        return allYears.stream().map(y -> years.contains(y) ? y : null).toList();
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

    public static class IndexGroupModel implements Comparable<IndexGroupModel> {
        private final String       name;
        private final int          index;
        private final List<String> years;

        public IndexGroupModel(String name, int index, List<String> years) {
            this.name  = name;
            this.index = index;
            this.years = years;
        }

        public String getName() {
            return name;
        }

        public List<String> getYears() {
            return years;
        }

        @Override
        public int compareTo(IndexGroupModel o) {
            return index == o.index ? String.CASE_INSENSITIVE_ORDER.compare(name, o.name) : Integer.compare(index, o.index);
        }
    }
}
