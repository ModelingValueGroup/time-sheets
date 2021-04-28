package nl.modelingvalue.timesheets.settings;

import java.util.ArrayList;
import java.util.List;

import de.micromata.jira.rest.core.domain.AuthorBean;
import de.micromata.jira.rest.core.domain.ProjectBean;
import de.micromata.jira.rest.core.domain.WorkEntryBean;

public class ProjectBucket {
    public boolean          ignore;
    public String           bucketName;
    public String           namePattern;
    public String           keyPattern;
    public List<UserBucket> userBuckets;

    public boolean isMatch(ProjectBean pb) {
        return !ignore &&
                (namePattern != null && pb.getName() != null && pb.getName().matches(namePattern))
                || (keyPattern != null && pb.getKey() != null && pb.getKey().matches(keyPattern));
    }

    public String findUserBucket(WorkEntryBean workEntryBean) {
        AuthorBean       author   = workEntryBean.getAuthor();
        List<UserBucket> matching = this.userBuckets.stream().filter(u -> u.isMatch(author)).toList();
        if (matching.isEmpty()) {
            return null;
        }
        if (1 < matching.size() && matching.get(matching.size() - 1).displayNamePattern.equals(".*")) {
            matching = new ArrayList<>(matching);
            matching.remove(matching.size() - 1);
        }
        if (matching.size() == 1) {
            return matching.get(0).bucketName;
        }
        throw new Error("multiple userbuckets match on " + workEntryBean.getAuthor().getDisplayName() + ": " + matching);
    }
}
