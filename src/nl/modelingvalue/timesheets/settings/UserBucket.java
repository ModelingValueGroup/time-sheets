package nl.modelingvalue.timesheets.settings;

import java.util.regex.Pattern;

import de.micromata.jira.rest.core.domain.AuthorBean;

public class UserBucket {
    public String bucketName;
    public String displayNamePattern;

    public boolean isMatch(AuthorBean ab) {
        return (bucketName != null && ab.getDisplayName() != null && ab.getDisplayName().matches(".*" + Pattern.quote(bucketName) + ".*"))
                || (displayNamePattern != null && ab.getDisplayName() != null && ab.getDisplayName().matches(displayNamePattern));
    }
}
