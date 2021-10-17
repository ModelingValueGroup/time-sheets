package nl.modelingvalue.timesheets.info;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import de.micromata.jira.rest.*;
import de.micromata.jira.rest.core.domain.*;
import de.micromata.jira.rest.core.util.*;
import nl.modelingvalue.timesheets.util.*;

import static java.lang.System.*;
import static nl.modelingvalue.timesheets.util.LogAccu.*;
import static nl.modelingvalue.timesheets.util.Pool.*;

public class ServerInfo extends Info {
    public  boolean           ignore;
    public  String            url;
    public  String            username;
    public  String            apiToken;
    //
    private JiraRestClient    jiraRestClient;
    private List<ProjectBean> projectList = new ArrayList<>();
    private List<AccountBean> accountList = new ArrayList<>();

    public ServerInfo() {
    }

    @Override
    public String toString() {
        return "jira@" + url;
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

    public List<AccountBean> getUserList() {
        return accountList;
    }

    private void setUserList(List<AccountBean> accountList) {
        this.accountList = accountList;
    }

    public void connectAndAskInfo() {
        debug(">>> connect to " + id);
        long t0 = System.currentTimeMillis();
        if (ignore) {
            debug("    ... server " + id + " is disabled");
        } else {
            try {
                final JiraRestClient jiraRestClient = new JiraRestClient(POOL);
                int                  response       = jiraRestClient.connect(new URI(url), username, apiToken);
                if (response != 200) {
                    throw new FatalException("could not connect to " + id + ": response=" + response);
                }
                setJiraRestClient(jiraRestClient);
                setProjects(getProjectsStream().toList());
                if (url.contains(".atlassian.net")) {
                    setUserList(getUsersStream().toList());
                }
            } catch (IOException | URISyntaxException | ExecutionException | InterruptedException e) {
                throw new Wrapper("could not connect to " + url, e);
            }
        }
        trace(String.format("%6d ms to connect to %s", currentTimeMillis() - t0, id));
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

    private Stream<AccountBean> getUsersStream() {
        return Yielder.stream(POOL, yielder -> {
            debug(">>>>>> users of " + id);
            long t0 = System.currentTimeMillis();

            CompletableFuture<List<AccountBean>> allUsersFuture = getJiraRestClient().getUserClient().getAllUsers(0, 1000);
            List<AccountBean>                    usersBeans     = waitFor(allUsersFuture);
            debug("       ... found " + usersBeans.size() + " users in " + id + ": " + usersBeans.stream().map(UserBean::getDisplayName).toList());
            yielder.yieldz(usersBeans);
            trace(String.format("%6d ms to download users for %s", currentTimeMillis() - t0, id));
            debug("<<<<<< users of " + id);
        });
    }
}
