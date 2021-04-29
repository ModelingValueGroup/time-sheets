package nl.modelingvalue.timesheets.model;

import java.time.LocalDateTime;

import de.micromata.jira.rest.core.domain.IssueBean;
import de.micromata.jira.rest.core.domain.ProjectBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;
import nl.modelingvalue.timesheets.settings.ProjectBucket;
import nl.modelingvalue.timesheets.settings.RepoBucket;

public record WorkInfo(
        RepoBucket repoBucket,
        ProjectBucket projectBucket,
        ProjectBean projectBean,
        IssueBean issueBean,
        WorkEntryBean workEntryBean
) {
    public String repoName() {
        return repoBucket.name;
    }

    public String projectBucketName() {
        return projectBucket.name;
    }

    public String userBucketName() {
        return projectBucket.findUserBucket(workEntryBean).name;
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
