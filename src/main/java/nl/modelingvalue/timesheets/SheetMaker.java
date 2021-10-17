package nl.modelingvalue.timesheets;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import com.google.gson.reflect.*;
import com.google.gson.stream.*;
import de.micromata.jira.rest.core.domain.*;
import de.micromata.jira.rest.core.util.*;
import nl.modelingvalue.timesheets.info.*;
import nl.modelingvalue.timesheets.model.*;
import nl.modelingvalue.timesheets.util.*;

import static nl.modelingvalue.timesheets.Config.*;
import static nl.modelingvalue.timesheets.util.LogAccu.*;
import static nl.modelingvalue.timesheets.util.Pool.*;

public class SheetMaker {
    public static final Type SHEETMAKER_TYPE = new TypeToken<SheetMaker>() {
    }.getType();

    public  Map<String, ServerInfo>  servers  = new HashMap<>();
    public  Map<String, PersonInfo>  persons  = new HashMap<>();
    public  Map<String, TeamInfo>    teams    = new HashMap<>();
    public  Map<String, GroupInfo>   groups   = new HashMap<>();
    public  Map<String, ProjectInfo> projects = new HashMap<>();
    public  PublishInfo              publish;
    private long                     supportCrc;

    public static SheetMaker read(String[] args) {
        Stream<String> pathNameStream = args.length == 0 ? DEFAULT_DIRS.stream() : Arrays.stream(args);
        List<Path>     paths          = pathNameStream.map(Paths::get).flatMap(fd -> U.selectJsonFiles(fd, DEFAULT_NAME_PAT)).toList();
        if (paths.isEmpty()) {
            throw new FatalException("no sheetMaker config files found, nothing to work on");
        }
        return paths.stream().map(SheetMaker::read).reduce(SheetMaker::merge).orElseGet(SheetMaker::new);
    }

    public void init() {
        servers.entrySet().removeIf(e -> e.getKey().startsWith("-"));
        persons.entrySet().removeIf(e -> e.getKey().startsWith("-"));
        teams.entrySet().removeIf(e -> e.getKey().startsWith("-"));
        groups.entrySet().removeIf(e -> e.getKey().startsWith("-"));
        projects.entrySet().removeIf(e -> e.getKey().startsWith("-"));

        teams.put("*", new TeamInfo("*", teams.size(), persons.values().stream().filter(pi -> !pi.ignore && !pi.retired).map(pi -> pi.id).toList()));

        servers.values().forEach(v -> v.init(this));
        persons.values().forEach(v -> v.init(this));
        teams.values().forEach(v -> v.init(this));
        groups.values().forEach(v -> v.init(this));
        projects.values().forEach(v -> v.init(this));

        publish.init(this);
    }

    public void connectAndAskInfo() {
        parallelExecAndWait(servers.values().stream(), ServerInfo::connectAndAskInfo);
        info("projects found:");
        servers.values()
                .stream()
                .sorted(Comparator.comparing(si -> si.id))
                .forEach(si -> si.getProjectList()
                        .stream()
                        .sorted(Comparator.comparing(ProjectBean::getKey))
                        .forEach(pb -> info(String.format("    %-12s  %-6s  %-6s  '%s'", si.id, pb.getKey(), pb.getId(), pb.getName()))));
    }

    private List<ProjectBean> getAllProjectBeans() {
        return servers.values().stream().flatMap(s -> s.getProjectList().stream()).toList();
    }

    public void resolveProjects() {
        List<ProjectBean> allProjectBeans = getAllProjectBeans();
        List<ProjectBean> matched = projects.values().stream()
                .flatMap(p -> p.resolveProject(allProjectBeans).stream())
                .toList();
        matched.stream()
                .collect(Collectors.groupingBy(i1 -> i1, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> 1 < e.getValue())
                .forEach(e -> err("the project " + e.getKey().getId() + " is matched " + e.getValue() + " times"));
        allProjectBeans.stream()
                .filter(pb -> !matched.contains(pb))
                .forEach(pb -> err("the project " + pb.getId() + " is not matched at all"));
    }

    public void resolveUsers() {
        servers.values().forEach(s -> {
            List<AccountBean> userList = s.getUserList();
            persons.values().forEach(p -> {
                List<AccountBean> matching = userList.stream()
                        .filter(ab -> !ab.getAccountType().equals("app"))
                        .filter(p::isMatch)
                        .toList();
                if (matching.isEmpty()) {
                    LogAccu.debug("person " + p + " does not seem to have an account on " + s);
                } else {
                    p.connectAccount(s, matching.get(0));
                    if (matching.size() != 1) {
                        LogAccu.info("person " + p + " seems to have multiple accounts on " + s + ": " + matching);
                    }
                }
            });
        });
    }

    public void checkProjectConsistency() {
        Map<ProjectBean, Long> counted = getProjectInfoStream()
                .map(ProjectInfo::getProjectBean)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        counted.entrySet().stream().filter(e -> 1 < e.getValue()).forEach(e -> err("project '" + e.getKey().getKey() + "' matches " + e.getValue() + " times"));
        Set<ProjectBean> unmatched = new HashSet<>(getAllProjectBeans());
        unmatched.removeAll(counted.keySet());
        unmatched.forEach(pb -> err("project '" + pb.getKey() + "' is never matched"));
    }

    private Stream<GroupInfo> getGroupInfoStream() {
        return groups.values().stream();
    }

    private Stream<ProjectInfo> getProjectInfoStream() {
        return projects.values().stream();
    }

    private SheetMaker merge(SheetMaker s2) {
        merge(servers, s2.servers, "servers");
        merge(persons, s2.persons, "persons");
        merge(teams, s2.teams, "teams");
        merge(groups, s2.groups, "groups");
        merge(projects, s2.projects, "projects");
        publish = publish != null ? publish : s2.publish;
        return this;
    }

    private <T> void merge(Map<String, T> map1, Map<String, T> map2, String name) {
        Set<String> overlappingKeys = new HashSet<>(map1.keySet());
        overlappingKeys.retainAll(map2.keySet());
        if (!overlappingKeys.isEmpty()) {
            throw new FatalException("sheetMaker config has double defines: " + overlappingKeys + " in " + name);
        }
        map1.putAll(map2);
    }

    private static SheetMaker read(Path f) {
        trace("...reading " + f.toAbsolutePath());
        try {
            return GsonUtils.withSpecials().fromJson(new JsonReader(Files.newBufferedReader(f)), SHEETMAKER_TYPE);
        } catch (IOException e) {
            throw new Wrapper("can not read " + f.toAbsolutePath(), e);
        }
    }

    public void downloadAllWorkItems() {
        Pool.parallelExecAndWait(getProjectInfoStream(), ProjectInfo::downloadAllWorkItems);
        getGroupInfoStream().forEach(GroupInfo::accumulateSubs);
    }

    public ServerInfo getServerBucketFor(ProjectBean project) {
        return servers.values().stream().filter(s -> s.getProjectList().contains(project)).findFirst().orElseThrow();
    }

    private <T extends Info> T resolve(String name, Map<String, T> map, String label) {
        T o = map.get(name);
        if (o == null) {
            throw new FatalException("no " + label + " with name " + name + " found");
        }
        return o;
    }

    public GroupInfo resolveGroup(String name) {
        return resolve(name, groups, "group");
    }

    public ProjectInfo resolveProject(String name) {
        return resolve(name, projects, "project");
    }

    public TeamInfo resolveTeam(String name) {
        return resolve(name, teams, "team");
    }

    public PersonInfo resolvePerson(String name) {
        return resolve(name, persons, "person");
    }

    public PersonInfo findPersonOrCreate(ServerInfo serverInfo, AccountBean ab) {
        List<PersonInfo> matching = persons.values().stream().filter(personInfo -> personInfo.isMatch(ab)).toList();
        return switch (matching.size()) {
            case 1 -> matching.get(0);
            case 0 -> {
                err("account '" + ab.getDisplayName() + "' does not match any person");
                PersonInfo newPerson = new PersonInfo(serverInfo, ab, this);
                persons.put(newPerson.id, newPerson);
                yield newPerson;
            }
            default -> throw new FatalException("multiple persons match '" + ab.getDisplayName() + "': " + matching.stream().map(pi -> pi.id).toList());
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
        publish.groupInfos.forEach(pi -> pi.getYears()
                .map(y -> new PageModel(pi, y))
                .toList()
                .stream()
                .filter(pm -> !CURRENT_YEAR_ONLY || LocalDate.now().getYear() == pm.year)
                .forEach(model -> {
                    String outFile = String.format(TIME_SHEET_FILENAME_TEMPLATE, model.year, model.pgInfo.id);
                    trace("> generating " + outFile);
                    generate(PAGE_HTML_TEMPLATE, outFile, model);
                }));
    }

    public void generate(String templateName, String outFileName, Model<?> model) {
        write(outFileName, new FreeMarkerEngine().process("nl/modelingvalue/timesheets/" + templateName + ".ftl", model));
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
            throw new Wrapper("can not write output file", e);
        }
    }
}
