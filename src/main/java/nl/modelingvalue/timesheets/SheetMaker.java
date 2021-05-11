package nl.modelingvalue.timesheets;

import static nl.modelingvalue.timesheets.Config.CURRENT_YEAR_ONLY;
import static nl.modelingvalue.timesheets.Config.DEFAULT_DIRS;
import static nl.modelingvalue.timesheets.Config.DEFAULT_NAME_PAT;
import static nl.modelingvalue.timesheets.Config.INDEX_FILENAME;
import static nl.modelingvalue.timesheets.Config.INDEX_HTML_TEMPLATE;
import static nl.modelingvalue.timesheets.Config.PAGE_HTML_TEMPLATE;
import static nl.modelingvalue.timesheets.Config.PUBLIC_DIRNAME;
import static nl.modelingvalue.timesheets.Config.RAW_DIRNAME;
import static nl.modelingvalue.timesheets.Config.SUPPORT_FILES;
import static nl.modelingvalue.timesheets.Config.TIME_SHEET_FILENAME_TEMPLATE;
import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.LogAccu.trace;
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
import nl.modelingvalue.timesheets.model.Model;
import nl.modelingvalue.timesheets.model.PageModel;
import nl.modelingvalue.timesheets.util.FreeMarkerEngine;
import nl.modelingvalue.timesheets.util.GsonUtils;
import nl.modelingvalue.timesheets.util.PageEncryptWrapper;
import nl.modelingvalue.timesheets.util.Pool;
import nl.modelingvalue.timesheets.util.U;

public class SheetMaker {
    public static final Type SHEETMAKER_TYPE = new TypeToken<SheetMaker>() {
    }.getType();

    public  PublishInfo             publish;
    public  Map<String, ServerInfo> servers = new HashMap<>();
    public  Map<String, PersonInfo> persons = new HashMap<>();
    public  Map<String, TeamInfo>   teams   = new HashMap<>();
    public  Map<String, PartInfo>   parts   = new HashMap<>();
    private long                    supportCrc;

    public void init() {
        // json can not read objects of different class, so we replace parts here with the actual part:
        parts = parts.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().makeActualPart()));

        persons.values().forEach(v -> v.init(this));
        teams.values().forEach(v -> v.init(this));
        parts.values().forEach(v -> v.init(this));
        publish.init(this);
    }

    public static SheetMaker read(String[] args) {
        Stream<String> pathNameStream = args.length == 0 ? DEFAULT_DIRS.stream() : Arrays.stream(args);
        List<Path>     paths          = pathNameStream.map(Paths::get).flatMap(fd -> U.selectJsonFiles(fd, DEFAULT_NAME_PAT)).toList();
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
        publish = publish != null ? publish : s2.publish;
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
        trace("...reading " + f.toAbsolutePath());
        try {
            return GsonUtils.withSpecials().fromJson(new JsonReader(Files.newBufferedReader(f)), SHEETMAKER_TYPE);
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
                PersonInfo newPerson = new PersonInfo(ab, this);
                persons.put(newPerson.id, newPerson);
                yield newPerson;
            }
            default -> throw new Error("multiple persons match '" + ab.getDisplayName() + "': " + matching.stream().map(pi -> pi.id).toList());
        };
    }

    public void generateIndex() {
        trace("> generating " + INDEX_FILENAME);
        generate(INDEX_HTML_TEMPLATE, INDEX_FILENAME, new IndexModel(this));
    }

    public void generateSupportFiles() {
        supportCrc = Stream.of(RAW_DIRNAME, PUBLIC_DIRNAME)
                .peek(d -> U.createDirectories(Paths.get(d)))
                .flatMap(d -> SUPPORT_FILES.stream().map(fn -> Paths.get(d, fn)))
                .mapToLong(U::copyResourceCrc)
                .reduce(0, (a, b) -> a ^ b);
    }

    public void generateAll() {
        parts.values().forEach(pi -> pi.yearPersonMonthInfo.budgetStatusLog(pi.id));//TODO remove later

        publish.partInfos.forEach(pi -> pi.getYears()
                .filter(y -> !CURRENT_YEAR_ONLY || LocalDate.now().getYear() == y)
                .forEach(year -> generate(pi, year)));
    }

    private void generate(PartInfo pi, Integer year) {
        String outFile = String.format(TIME_SHEET_FILENAME_TEMPLATE, year, pi.id);
        trace("> generating " + outFile);
        generate(PAGE_HTML_TEMPLATE, outFile, new PageModel(pi, year));
    }

    public void generate(String templateName, String outFileName, Model<?> projectModel) {
        write(outFileName, new FreeMarkerEngine().process("nl/modelingvalue/timesheets/" + templateName + ".ftl", projectModel));
    }

    private void write(String outputFile, String page) {
        Path rawFile    = Paths.get(RAW_DIRNAME, outputFile);
        Path rawCrcFile = Paths.get(RAW_DIRNAME, outputFile + ".crc.json");
        Path pubFile    = Paths.get(PUBLIC_DIRNAME, outputFile);
        Path pubCrcFile = Paths.get(PUBLIC_DIRNAME, outputFile + ".crc.json");
        try {
            Files.createDirectories(rawFile.getParent());
            String crcJson = U.makeCrcJson(supportCrc ^ U.writeStringCrc(rawFile, page));
            U.writeStringCrc(rawCrcFile, crcJson);
            if (publish.password != null) {
                new PageEncryptWrapper(publish.password).write(page, pubFile);
                U.writeStringCrc(pubCrcFile, crcJson);
            }
        } catch (IOException e) {
            throw new Error("can not write output file", e);
        }
    }

}
