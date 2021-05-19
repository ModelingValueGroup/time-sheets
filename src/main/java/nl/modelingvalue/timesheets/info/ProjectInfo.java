package nl.modelingvalue.timesheets.info;

import static de.micromata.jira.rest.core.jql.EField.AFFECTED_VERSION;
import static de.micromata.jira.rest.core.jql.EField.PROJECT;
import static de.micromata.jira.rest.core.jql.EOperator.EQUALS;
import static de.micromata.jira.rest.core.jql.EOperator.GREATER_THAN_EQUALS;
import static java.lang.System.currentTimeMillis;
import static nl.modelingvalue.timesheets.util.Jql.DATE_FORMATTER;
import static nl.modelingvalue.timesheets.util.LogAccu.debug;
import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.LogAccu.trace;
import static nl.modelingvalue.timesheets.util.Pool.POOL;
import static nl.modelingvalue.timesheets.util.Pool.parallelExecAndWait;
import static nl.modelingvalue.timesheets.util.Pool.waitFor;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import de.micromata.jira.rest.client.SearchClient;
import de.micromata.jira.rest.core.domain.IssueBean;
import de.micromata.jira.rest.core.domain.JqlSearchResult;
import de.micromata.jira.rest.core.domain.ProjectBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;
import de.micromata.jira.rest.core.domain.WorklogBean;
import de.micromata.jira.rest.core.jql.EField;
import de.micromata.jira.rest.core.jql.JqlBuilder;
import de.micromata.jira.rest.core.jql.JqlBuilder.JqlKeyword;
import de.micromata.jira.rest.core.jql.JqlSearchBean;
import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.SheetMaker;
import nl.modelingvalue.timesheets.util.FatalException;
import nl.modelingvalue.timesheets.util.LogAccu;
import nl.modelingvalue.timesheets.util.U;
import nl.modelingvalue.timesheets.util.Yielder;

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
                PersonInfo person = sheetMaker.findPersonOrCreate(wb.getAuthor());
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
