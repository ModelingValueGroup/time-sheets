package nl.modelingvalue.timesheets.info;

import static java.lang.System.currentTimeMillis;
import static nl.modelingvalue.timesheets.util.LogAccu.trace;
import static nl.modelingvalue.timesheets.util.LogAccu.debug;
import static nl.modelingvalue.timesheets.util.Pool.POOL;
import static nl.modelingvalue.timesheets.util.Pool.waitFor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import de.micromata.jira.rest.JiraRestClient;
import de.micromata.jira.rest.core.domain.ProjectBean;
import nl.modelingvalue.timesheets.util.Yielder;

public class ServerInfo extends Info {
    public  boolean           ignore;
    public  String            url;
    public  String            username;
    public  String            apiToken;
    //
    private JiraRestClient    jiraRestClient;
    private List<ProjectBean> projectList = new ArrayList<>();

    public ServerInfo() {
    }

    public JiraRestClient getJiraRestClient() {
        return jiraRestClient;
    }

    private void setJiraRestClient(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    public List<ProjectBean> getProjectList() {
        return projectList;
    }

    private void setProjects(List<ProjectBean> projectList) {
        this.projectList = projectList;
    }

    public void connectAndAskProjects() {
        debug(">>> connect to " + id);
        long t0 = System.currentTimeMillis();
        if (ignore) {
            debug("    ... server " + id + " is disabled");
        } else {
            try {
                final JiraRestClient jiraRestClient = new JiraRestClient(POOL);
                int                  response       = jiraRestClient.connect(new URI(url), username, apiToken);
                if (response != 200) {
                    debug("### connect to " + id + " failed (" + response + ")");
                    throw new Error("could not connect: response=" + response);
                }
                setJiraRestClient(jiraRestClient);
                setProjects(getProjectsStream().toList());
            } catch (IOException | URISyntaxException | ExecutionException | InterruptedException e) {
                throw new Error("could not connect to " + url, e);
            }
        }
        trace(String.format("%6d ms to connect to %s" , currentTimeMillis() - t0, id));
        debug("<<< connect to " + id);
    }

    private Stream<ProjectBean> getProjectsStream() {
        return Yielder.stream(POOL, yielder -> {
            debug(">>>>>> projects of " + id);
            long t0 = System.currentTimeMillis();

            CompletableFuture<List<ProjectBean>> allProjectsFuture = getJiraRestClient().getProjectClient().getAllProjects();
            List<ProjectBean>                    projectBeans      = waitFor(allProjectsFuture);
            debug("       ... found " + projectBeans.size() + " projects in " + id + ": " + projectBeans.stream().map(ProjectBean::getKey).toList());
            yielder.yieldz(projectBeans);
            trace(String.format("%6d ms to download projects for %s", currentTimeMillis() - t0, id));
            debug("<<<<<< projects of " + id);
        });
    }
}
