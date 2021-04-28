package nl.modelingvalue.timesheets.model;

import java.time.LocalDateTime;

import de.micromata.jira.rest.core.domain.IssueBean;
import de.micromata.jira.rest.core.domain.ProjectBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;
import nl.modelingvalue.timesheets.settings.JiraBucket;
import nl.modelingvalue.timesheets.settings.ProjectBucket;

public record WorkInfo(
        JiraBucket jiraBucket,
        ProjectBucket projectBucket,
        ProjectBean projectBean,
        IssueBean issueBean,
        WorkEntryBean workEntryBean
) {
    public String jiraName() {
        return jiraBucket.name;
    }

    public String projectBucketName() {
        return projectBucket.bucketName;
    }

    public String userBucketName() {
        return projectBucket.findUserBucket(workEntryBean);
    }

    public String authorName() {
        return workEntryBean.getAuthor().getDisplayName();
    }

    public LocalDateTime date() {
        return workEntryBean.getStartedDate();
    }

    public int year() {
        return date().getYear();
    }

    public int month() {
        return date().getMonthValue();
    }

    public long seconds() {
        return workEntryBean.getTimeSpentSeconds();
    }
}
