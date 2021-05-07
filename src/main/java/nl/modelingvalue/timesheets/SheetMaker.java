package nl.modelingvalue.timesheets;

import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.LogAccu.info;
import static nl.modelingvalue.timesheets.util.Pool.parallelExecAndWait;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.micromata.jira.rest.core.domain.AccountBean;
import de.micromata.jira.rest.core.domain.ProjectBean;
import nl.modelingvalue.timesheets.info.PageInfo;
import nl.modelingvalue.timesheets.info.PartInfo;
import nl.modelingvalue.timesheets.info.PersonInfo;
import nl.modelingvalue.timesheets.info.ProjectInfo;
import nl.modelingvalue.timesheets.info.PublishInfo;
import nl.modelingvalue.timesheets.info.ServerInfo;
import nl.modelingvalue.timesheets.info.TeamInfo;
import nl.modelingvalue.timesheets.model.IndexModel;
import nl.modelingvalue.timesheets.model.PageModel;
import nl.modelingvalue.timesheets.util.FreeMarker;
import nl.modelingvalue.timesheets.util.GsonUtils;
import nl.modelingvalue.timesheets.util.Pool;
import nl.modelingvalue.timesheets.util.U;

public class SheetMaker {
    public static final Type SETTINGS_TYPE = new TypeToken<SheetMaker>() {
    }.getType();

    public PublishInfo             publish;
    public Map<String, ServerInfo> servers = new HashMap<>();
    public Map<String, PersonInfo> persons = new HashMap<>();
    public Map<String, TeamInfo>   teams   = new HashMap<>();
    public Map<String, PartInfo>   parts   = new HashMap<>();

    public void init() {
        // json can not read objects of different class, so we replace parts here with the actual part:
        parts = parts.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().makeActualPart()));

        persons.values().forEach(v -> v.init(this));
        teams.values().forEach(v -> v.init(this));
        parts.values().forEach(v -> v.init(this));
        publish.init(this);
    }

    public static SheetMaker read(String[] args) {
        Stream<String> pathNameStream = args.length == 0 ? Config.DEFAULT_DIRS.stream() : Arrays.stream(args);
        List<Path>     paths          = pathNameStream.map(Paths::get).flatMap(fd -> U.selectJsonFiles(fd, Config.DEFAULT_NAME_PAT)).toList();
        if (paths.isEmpty()) {
            throw new Error("no sheetMaker files found, nothing to work on");
        }
        return paths.stream().map(SheetMaker::read).reduce(SheetMaker::merge).orElseGet(SheetMaker::new);
    }

    public void connectAndAskProjects() {
        servers.values().forEach(v -> v.init(this));
        parallelExecAndWait(servers.values().stream(), ServerInfo::connectAndAskProjects);
    }

    public void matchPartsToProjects() {
        parts.values().forEach(p -> p.matchPartsToProjects(getAllProjectBeans()));
    }

    private List<ProjectBean> getAllProjectBeans() {
        return servers.values().stream().flatMap(s -> s.getProjectList().stream()).toList();
    }

    public void checkProjectConsistency() {
        Map<ProjectBean, Long> counted = getProjectInfoParts()
                .map(ProjectInfo::getProjectBean)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        counted.entrySet().stream().filter(e -> 1 < e.getValue()).forEach(e -> err("project '" + e.getKey().getKey() + "' matches " + e.getValue() + " times"));
        Set<ProjectBean> unmatched = new HashSet<>(getAllProjectBeans());
        unmatched.removeAll(counted.keySet());
        unmatched.forEach(pb -> err("project '" + pb.getKey() + "' is never matched"));
    }

    private Stream<ProjectInfo> getProjectInfoParts() {
        return parts.values()
                .stream()
                .map(part -> part instanceof ProjectInfo ? ((ProjectInfo) part) : null)
                .filter(Objects::nonNull);
    }

    private Stream<PageInfo> getPageInfoParts() {
        return parts.values()
                .stream()
                .map(part -> part instanceof PageInfo ? ((PageInfo) part) : null)
                .filter(Objects::nonNull);
    }

    private SheetMaker merge(SheetMaker s2) {
        merge(servers, s2.servers, "servers");
        merge(persons, s2.persons, "persons");
        merge(teams, s2.teams, "teams");
        merge(parts, s2.parts, "parts");
        return this;
    }

    private <T> void merge(Map<String, T> map1, Map<String, T> map2, String name) {
        Set<String> keys = new HashSet<>(map1.keySet());
        keys.retainAll(map2.keySet());
        if (!keys.isEmpty()) {
            throw new Error("sheetMaker member double define: " + keys + " in " + name);
        }
        map1.putAll(map2);
    }

    private static SheetMaker read(Path f) {
        System.err.println("...reading " + f.toAbsolutePath());
        try {
            return GsonUtils.withSpecials().fromJson(new JsonReader(Files.newBufferedReader(f)), SETTINGS_TYPE);
        } catch (IOException e) {
            throw new Error("can not read " + f.toAbsolutePath(), e);
        }
    }

    public void downloadAllWorkItems() {
        Pool.parallelExecAndWait(getProjectInfoParts(), ProjectInfo::downloadAllWorkItems);
        getPageInfoParts().forEach(PageInfo::accumulateSubs);
    }

    public ServerInfo getServerBucketFor(ProjectBean project) {
        return servers.values().stream().filter(s -> s.getProjectList().contains(project)).findFirst().orElseThrow();
    }

    public PartInfo mustFindPart(String pn) {
        return parts.values().stream().filter(pi -> pi.id.equals(pn)).findFirst().orElseThrow(() -> new Error("no part with name " + pn + " found"));
    }

    public PersonInfo mustFindPerson(String pn) {
        return persons.values().stream().filter(pi -> pi.id.equals(pn)).findFirst().orElseThrow(() -> new Error("no person with name " + pn + " found"));
    }

    public PersonInfo findPersonOrCreate(AccountBean ab) {
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
        publish.partInfos.forEach(pi -> {
            pi.getYears().filter(y -> !Config.CURRENT_YEAR_ONLY || LocalDate.now().getYear() == y).forEach(year -> {
                String outFile = String.format(Config.TIME_SHEET_FILENAME_TEMPLATE, year, pi.id);
                info("generating " + outFile);
                FreeMarker.generate("page.html", outFile, new PageModel(pi, year), publish.password);
            });
        });
        new IndexModel(this).generate();
    }
}
