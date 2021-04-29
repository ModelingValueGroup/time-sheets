package nl.modelingvalue.timesheets.settings;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.micromata.jira.rest.core.domain.AuthorBean;
import de.micromata.jira.rest.core.domain.ProjectBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;
import nl.modelingvalue.timesheets.util.U;

public class ProjectBucket {
    public boolean                 ignore;
    public String                  repoName;
    public String                  projectName;
    public String                  projectKey;
    public long                    budget;
    public Map<String, UserBucket> userBuckets;

    public  int     year;
    public  String  name;
    private Pattern repoNamePat;
    private Pattern projectNamePat;
    private Pattern projectKeyPat;

    public void init(int year, String name) {
        this.year = year;
        this.name = name;
        if (projectName == null && projectKey == null) {
            projectName = name;
        }
        if (repoNamePat == null) {
            repoNamePat = U.cachePattern(repoName);
        }
        if (projectNamePat == null) {
            projectNamePat = U.cachePattern(projectName);
        }
        if (projectKeyPat == null) {
            projectKeyPat = U.cachePattern(projectKey);
        }
        userBuckets.forEach((n, u) -> u.init(n));
    }

    public boolean isMatch(RepoBucket repo, ProjectBean pb) {
        if (ignore) {
            return false;
        }
        return (repoName == null || repoNamePat.matcher(repo.name).matches())
                && (projectName == null || projectNamePat.matcher(pb.getName()).matches())
                && (projectKey == null || projectKeyPat.matcher(pb.getKey()).matches());
    }

    public UserBucket findUserBucket(WorkEntryBean workEntryBean) {
        AuthorBean       author   = workEntryBean.getAuthor();
        List<UserBucket> matching = this.userBuckets.values().stream().filter(u -> u.isMatch(author) && workEntryBean.getStartedDate().getYear() == year).toList();
        if (matching.size() == 0) {
            return null;
        } else if (matching.size() == 1) {
            return matching.get(0);
        }
        List<UserBucket> matchingEx = matching.stream().filter(u -> !u.isCatchAll()).toList();
        if (matchingEx.size() == 1) {
            return matchingEx.get(0);
        } else {
            throw new Error("multiple userbuckets match on " + workEntryBean.getAuthor().getDisplayName() + ": " + matching);
        }
    }
}
