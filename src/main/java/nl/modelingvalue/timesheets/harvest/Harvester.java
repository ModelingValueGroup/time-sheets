package nl.modelingvalue.timesheets.harvest;

import static de.micromata.jira.rest.core.jql.EField.CREATED;
import static de.micromata.jira.rest.core.jql.EField.PROJECT;
import static de.micromata.jira.rest.core.jql.EOperator.EQUALS;
import static de.micromata.jira.rest.core.jql.SortOrder.ASC;
import static nl.modelingvalue.timesheets.util.Pool.POOL;
import static nl.modelingvalue.timesheets.util.Pool.executeInParallel;
import static nl.modelingvalue.timesheets.util.Pool.waitFor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import de.micromata.jira.rest.JiraRestClient;
import de.micromata.jira.rest.client.SearchClient;
import de.micromata.jira.rest.core.domain.IssueBean;
import de.micromata.jira.rest.core.domain.JqlSearchResult;
import de.micromata.jira.rest.core.domain.ProjectBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;
import de.micromata.jira.rest.core.domain.WorklogBean;
import de.micromata.jira.rest.core.jql.EField;
import de.micromata.jira.rest.core.jql.JqlBuilder;
import de.micromata.jira.rest.core.jql.JqlSearchBean;
import nl.modelingvalue.timesheets.model.TimeAdminModel;
import nl.modelingvalue.timesheets.model.WorkInfo;
import nl.modelingvalue.timesheets.settings.ProjectBucket;
import nl.modelingvalue.timesheets.settings.RepoBucket;
import nl.modelingvalue.timesheets.settings.Settings;
import nl.modelingvalue.timesheets.settings.YearBucket;
import nl.modelingvalue.timesheets.util.Yielder;

public class Harvester {
    private static final boolean TRACE = Boolean.getBoolean("Harvester.TRACE");

    private final RepoBucket              repoBucket;
    private final Map<String, YearBucket> yearsMap;
    private       JiraRestClient          jiraRestClient;

    public static void harvest(Settings settings, TimeAdminModel timeadmin) {
        List<Harvester> harvesters = settings.reposMap.values().stream()
                .filter(j -> !j.ignore)
                .map(r -> new Harvester(r, settings.yearsMap))
                .toList();

        Future<?> futureOverall = POOL.submit(() -> {
                    Stream<Harvester> harvestersStream = harvesters.stream();
                    executeInParallel(harvestersStream, h -> {
                        h.connect();
                        Stream<ProjectBean> projectsStream = h.getProjectsStream();
                        executeInParallel(projectsStream, p -> {
                            long                t0             = System.currentTimeMillis();
                            List<ProjectBucket> projectBuckets = h.findProjectBuckets(p);
                            executeInParallel(h.getIssuesStream(p), i -> {
                                Stream<WorkEntryBean> workEntriesStream = h.getWorkEntries(p, i);
                                executeInParallel(workEntriesStream, w ->
                                        projectBuckets.stream().filter(pb -> pb.year == w.getStartedDate().getYear()).findFirst().ifPresent(projectBucket -> timeadmin.add(new WorkInfo(h.repoBucket, projectBucket, p, i, w)))
                                );
                            });
                            System.err.printf("... %20.3f sec for %s\n", (System.currentTimeMillis() - t0) / 1000.0, p.getName());
                        });
                    });
                }
        );
        waitFor(futureOverall);
    }

    private Harvester(RepoBucket repoBucket, Map<String, YearBucket> yearsMap) {
        this.repoBucket = repoBucket;
        this.yearsMap   = yearsMap;
    }

    private List<ProjectBucket> findProjectBuckets(ProjectBean projectBean) {
        return yearsMap.values().stream().flatMap(y -> y.values().stream()).filter(pat -> pat.isMatch(repoBucket, projectBean)).toList();
    }

    private void connect() {
        if (TRACE) {
            System.err.println(Thread.currentThread().getName() + "- >>> connect  -  " + repoBucket.name);
        }
        try {
            final JiraRestClient jiraRestClient = new JiraRestClient(POOL);
            int                  response       = jiraRestClient.connect(new URI(repoBucket.url), repoBucket.username, repoBucket.apiToken);
            if (response != 200) {
                throw new Error("could not connect: response=" + response);
            }
            this.jiraRestClient = jiraRestClient;
        } catch (IOException | URISyntaxException | ExecutionException | InterruptedException e) {
            throw new Error("could not connect to " + repoBucket.url, e);
        }
        if (TRACE) {
            System.err.println(Thread.currentThread().getName() + "- <<< connect  -  " + repoBucket.name);
        }
    }

    private Stream<ProjectBean> getProjectsStream() {
        return Yielder.stream(POOL, yielder -> {
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "  - >>> projects  -  " + repoBucket.name);
            }
            CompletableFuture<List<ProjectBean>> allProjectsFuture   = jiraRestClient.getProjectClient().getAllProjects();
            List<ProjectBean>                    projectBeans        = waitFor(allProjectsFuture);
            List<ProjectBean>                    enabledProjectBeans = projectBeans.stream().filter(p -> yearsMap.values().stream().flatMap(y -> y.values().stream()).anyMatch(pb -> pb.isMatch(repoBucket, p))).toList();
            yielder.yieldz(enabledProjectBeans);
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "  - ............... found " + enabledProjectBeans.size() + " projects in " + repoBucket.name);
            }
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "  - <<< projects  -  " + repoBucket.name);
            }
        });
    }

    private Stream<IssueBean> getIssuesStream(ProjectBean project) {
        return Yielder.stream(POOL, yielder -> {
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "      - >>> issues    -  " + repoBucket.name + "." + project.getKey());
            }

            SearchClient    searchClient = jiraRestClient.getSearchClient();
            String          jql          = new JqlBuilder().addCondition(PROJECT, EQUALS, project.getName()).orderBy(ASC, CREATED);
            JqlSearchBean   jsb          = new JqlSearchBean(jql).addField(EField.AFFECTED_VERSION).setStartAt(0);
            JqlSearchResult jqlSearchResult;
            do {
                jqlSearchResult = waitFor(searchClient.searchIssues(jsb));
                List<IssueBean> issues = jqlSearchResult.getIssues();
                yielder.yieldz(issues);
                if (TRACE) {
                    System.err.println(Thread.currentThread().getName() + "      - ............... found " + issues.size() + " issues for " + project.getKey());
                }
                jsb.setStartAt(jqlSearchResult.getStartAt() + issues.size());
            } while (jsb.getStartAt() < jqlSearchResult.getTotal());

            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "      - <<< issues    -  " + repoBucket.name + "." + project.getKey());
            }
        });
    }

    private Stream<WorkEntryBean> getWorkEntries(ProjectBean project, IssueBean issue) {
        return Yielder.stream(POOL, yielder -> {
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "      - >>> entries   -  " + repoBucket.name + "." + project.getKey() + "." + issue.getKey());
            }

            String                         id            = issue.getId();
            CompletableFuture<WorklogBean> worklogFuture = jiraRestClient.getIssueClient().getWorklogByIssue(id);
            WorklogBean                    worklogBean   = waitFor(worklogFuture);
            List<WorkEntryBean>            worklogs      = worklogBean.getWorklogs();
            if (worklogBean.getMaxResults() < worklogBean.getTotal()) {
                throw new Error("did not get all worklogs!!!");
            }
            yielder.yieldz(worklogs);
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "      - ............... found " + worklogs.size() + " worklogs in " + repoBucket.name + "." + project.getKey() + "." + issue.getKey());
            }
            if (TRACE) {
                System.err.println(Thread.currentThread().getName() + "      - <<< entries   -  " + repoBucket.name + "." + project.getKey() + "." + issue.getKey());
            }
        });
    }
}
