package nl.modelingvalue.timesheets.info;

import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.Pool.parallelExecAndWait;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.micromata.jira.rest.core.domain.AccountBean;
import de.micromata.jira.rest.core.domain.ProjectBean;
import nl.modelingvalue.timesheets.model.IndexModel;
import nl.modelingvalue.timesheets.model.PageModel;
import nl.modelingvalue.timesheets.util.FreeMarker;
import nl.modelingvalue.timesheets.util.GsonUtils;
import nl.modelingvalue.timesheets.util.Pool;

public class Settings {
    private static final List<String> DEFAULT_DIRS     = List.of(".", "..", System.getProperty("user.home"));
    private static final Pattern      DEFAULT_NAME_PAT = Pattern.compile("^time-sheets-.*.json$");
    public static final  Type         SETTINGS_TYPE    = new TypeToken<Settings>() {
    }.getType();

    public Map<String, ServerInfo>  servers  = new HashMap<>();
    public Map<String, PersonInfo>  persons  = new HashMap<>();
    public Map<String, TeamInfo>    teams    = new HashMap<>();
    public Map<String, ProjectInfo> projects = new HashMap<>();
    public Map<String, PageInfo>    pages    = new HashMap<>();

    public void init() {
        servers.values().forEach(v -> v.init(this));
        persons.values().forEach(v -> v.init(this));
        teams.values().forEach(v -> v.init(this));
        projects.values().forEach(v -> v.init(this));
        pages.values().forEach(v -> v.init(this));
    }

    public static Settings read(String[] args) {
        Stream<String> pathNameStream = args.length == 0 ? DEFAULT_DIRS.stream() : Arrays.stream(args);
        List<Path>     paths          = pathNameStream.map(Paths::get).flatMap(Settings::selectJsonFiles).toList();
        if (paths.isEmpty()) {
            throw new Error("no settings files found, nothing to work on");
        }
        Settings settings = paths.stream().map(Settings::read).reduce(Settings::merge).orElseGet(Settings::new);
        settings.init();
        return settings;
    }

    public void connect() {
        parallelExecAndWait(servers.values().stream(), ServerInfo::connect);
    }

    public void downloadAllProjects() {
        projects.values().forEach(p -> p.init2(getAllProjectBeans()));
    }

    private List<ProjectBean> getAllProjectBeans() {
        return servers.values().stream().flatMap(s -> s.getProjectList().stream()).toList();
    }

    public void checkProjectConsistency() {
        Map<ProjectBean, Long> counted = projects.values().stream().map(ProjectInfo::getProjectBean).filter(Objects::nonNull).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        counted.entrySet().stream().filter(e -> 1 < e.getValue()).forEach(e -> err("project '" + e.getKey().getKey() + "' matches " + e.getValue() + " times"));
        Set<ProjectBean> unmatched = new HashSet<>(getAllProjectBeans());
        unmatched.removeAll(counted.keySet());
        unmatched.forEach(pb -> err("project '" + pb.getKey() + "' is never matched"));
    }

    private Settings merge(Settings s2) {
        merge(servers, s2.servers, "servers");
        merge(persons, s2.persons, "persons");
        merge(teams, s2.teams, "teams");
        merge(projects, s2.projects, "projects");
        merge(pages, s2.pages, "pages");
        return this;
    }

    private <T> void merge(Map<String, T> map1, Map<String, T> map2, String name) {
        Set<String> keys = new HashSet<>(map1.keySet());
        keys.retainAll(map2.keySet());
        if (!keys.isEmpty()) {
            throw new Error("settings member double define: " + keys + " in " + name);
        }
        map1.putAll(map2);
    }

    private static Settings read(Path f) {
        System.err.println("...reading " + f.toAbsolutePath());
        try {
            return GsonUtils.withSpecials().fromJson(new JsonReader(Files.newBufferedReader(f)), SETTINGS_TYPE);
        } catch (IOException e) {
            throw new Error("can not read " + f.toAbsolutePath(), e);
        }
    }

    private static Stream<Path> selectJsonFiles(Path fd) {
        try {
            if (Files.isRegularFile(fd) && fd.getFileName().toString().endsWith(".json")) {
                return Stream.of(fd);
            }
            if (Files.isDirectory(fd)) {
                return Files.list(fd).filter(f -> DEFAULT_NAME_PAT.matcher(f.getFileName().toString()).matches());
            }
            System.err.println("WARNING: not a file or dir: " + fd.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("WARNING: dir " + fd.toAbsolutePath() + " can not be scanned for settings files (" + e + ")");
        }
        return Stream.empty();
    }

    public void downloadAllWorkItems() {
        Pool.parallelExecAndWait(projects.values().stream(), ProjectInfo::downloadAllWorkItems);
    }

    public ServerInfo getServerBucketFor(ProjectBean project) {
        return servers.values().stream().filter(s -> s.getProjectList().contains(project)).findFirst().orElseThrow();
    }

    public PersonInfo findPerson(AccountBean ab) {
        List<PersonInfo> matching = persons.values().stream().filter(personInfo -> personInfo.isMatch(ab)).toList();
        return switch (matching.size()) {
            case 1 -> matching.get(0);
            case 0 -> {
                err("account '" + ab.getDisplayName() + "' does not match any person");
                yield new PersonInfo(ab, this);
            }
            default -> throw new Error("multiple persons match '" + ab.getDisplayName() + "': " + matching.stream().map(pi -> pi.id).toList());
        };
    }

    public void generateAll() {
        pages.values().forEach(pageInfo -> {
            pageInfo.getYears().forEach(year -> {
                String outFile = String.format("timesheet-%4d-%s.html", year, pageInfo.id);
                FreeMarker.generate("page.html", outFile, new PageModel(pageInfo, year));
            });
        });
        new IndexModel(this).generate();
    }
}
