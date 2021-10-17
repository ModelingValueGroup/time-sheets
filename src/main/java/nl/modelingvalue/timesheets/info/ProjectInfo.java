package nl.modelingvalue.timesheets.info;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import de.micromata.jira.rest.client.*;
import de.micromata.jira.rest.core.domain.*;
import de.micromata.jira.rest.core.jql.*;
import de.micromata.jira.rest.core.jql.JqlBuilder.*;
import nl.modelingvalue.timesheets.*;
import nl.modelingvalue.timesheets.util.*;

import static de.micromata.jira.rest.core.jql.EField.*;
import static de.micromata.jira.rest.core.jql.EOperator.*;
import static java.lang.System.*;
import static nl.modelingvalue.timesheets.util.Jql.*;
import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.LogAccu.*;
import static nl.modelingvalue.timesheets.util.Pool.*;

public class ProjectInfo extends PGInfo {
    public ProjectBean projectBean;
    public ServerInfo  serverInfo;

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
    }

    public List<ProjectBean> resolveProject(List<ProjectBean> allProjectBeans) {
        List<ProjectBean> matching = allProjectBeans.stream().filter(pb -> pb.getKey().equals(id)).toList();
        switch (matching.size()) {
        case 1 -> {
            projectBean = matching.get(0);
            serverInfo  = sheetMaker.getServerBucketFor(projectBean);
        }
        case 0 -> err("no project matches '" + id + "'");
        default -> err("multiple projects match '" + id + "': " + matching.stream().map(pb -> pb.getKey() + "=(" + pb.getName() + ")").toList());
        }
        return matching;
    }

    @Override
    public void accumulateSubs() {
        // no subs, nothing to do here
    }

    public ProjectBean getProjectBean() {
        return projectBean;
    }

    public Stream<ProjectInfo> allProjectInfosDeep() {
        return Stream.of(this);
    }

    public Stream<PGInfo> allSubInfos() {
        return Stream.of(this);
    }

    public void downloadAllWorkItems() {
        if (serverInfo != null) {
            debug(">>>>>>>>>>>> issues and worklogs   -  " + fullName());
            long t0 = currentTimeMillis();
            parallelExecAndWait(getIssuesStream(), issue -> getWorkEntries(issue).forEach(wb -> {
                PersonInfo person = sheetMaker.findPersonOrCreate(serverInfo, wb.getAuthor());
                if (!person.ignore) {
                    int  year  = wb.getStartedDate().getYear();
                    int  month = wb.getStartedDate().getMonthValue();
                    long sec   = wb.getTimeSpentSeconds();
                    if (U.hoursFromSec(sec) < 0.25 && 2015 < year) {
                        LogAccu.err(String.format("extremely short work item detected: %5d sec for %-12s issue %-12s at %s (probably a human entry error!)", sec, person.id, issue.getKey(), wb.getStartedDate()));
                    }
                    yearPersonMonthInfo.add(person, year, month, new DetailInfo(sec, 0));
                }
            }));
            trace(String.format("%6d ms to download entries and worklogs of %s", currentTimeMillis() - t0, fullName()));
            debug("<<<<<<<<<<<< entries and worklogs   -  " + fullName());
        }
    }

    private Stream<IssueBean> getIssuesStream() {
        return Yielder.stream(POOL, yielder -> {
            debug(">>>>>>>>> issues    -  " + fullName());
            long t0 = currentTimeMillis();

            SearchClient    searchClient = serverInfo.getJiraRestClient().getSearchClient();
            JqlSearchBean   jsb          = new JqlSearchBean(buildAllIssuesJql()).addField(AFFECTED_VERSION).setStartAt(0);
            JqlSearchResult jqlSearchResult;
            do {
                jqlSearchResult = waitFor(searchClient.searchIssues(jsb));
                List<IssueBean> issues = jqlSearchResult.getIssues();
                yielder.yieldz(issues);
                debug("         ... found " + issues.size() + " (more) issues for " + fullName());
                jsb.setStartAt(jqlSearchResult.getStartAt() + issues.size());
            } while (jsb.getStartAt() < jqlSearchResult.getTotal());

            trace(String.format("%6d ms to download issues of %s", currentTimeMillis() - t0, fullName()));
            debug("<<<<<<<<< issues    -  " + fullName());
        });
    }

    private String buildAllIssuesJql() {
        JqlKeyword jqlKeyword = new JqlBuilder().addCondition(PROJECT, EQUALS, projectBean.getName());
        if (Config.CURRENT_YEAR_ONLY) {
            LocalDate updatedSince = LocalDate.now().withDayOfYear(1).minusMonths(1); // be careful and ga back to one month before the beginning of this year
            jqlKeyword = jqlKeyword.and().addCondition(EField.WORKLOG_DATE, GREATER_THAN_EQUALS, updatedSince.format(DATE_FORMATTER));
        }
        // for debugging a certain year:
        //        int year = 2017;
        //        jqlKeyword = jqlKeyword.and().addCondition(EField.WORKLOG_DATE, GREATER_THAN_EQUALS, LocalDate.of(year, 1, 1).minusMonths(1).format(DATE_FORMATTER));
        //        jqlKeyword = jqlKeyword.and().addCondition(EField.WORKLOG_DATE, LESS_THAN_EQUALS , LocalDate.of(year, 12, 31).plusMonths(1).format(DATE_FORMATTER));
        return jqlKeyword.build();
    }

    private Stream<WorkEntryBean> getWorkEntries(IssueBean issue) {
        return Yielder.stream(POOL, yielder -> {
            String                         id            = issue.getId();
            CompletableFuture<WorklogBean> worklogFuture = serverInfo.getJiraRestClient().getIssueClient().getWorklogByIssue(id);
            WorklogBean                    worklogBean   = waitFor(worklogFuture);
            List<WorkEntryBean>            worklogs      = worklogBean.getWorklogs();
            if (worklogBean.getMaxResults() < worklogBean.getTotal()) {
                throw new FatalException("did not get all worklogs (got " + worklogBean.getMaxResults() + " while total is " + worklogBean.getTotal() + ")");
            }
            if (Config.CURRENT_YEAR_ONLY) {
                worklogs.removeIf(web -> web.getStartedDate().getYear() < LocalDate.now().getYear());
            }
            yielder.yieldz(worklogs);
        });
    }

    private String fullName() {
        return serverInfo.id + "." + projectBean.getKey();
    }
}
