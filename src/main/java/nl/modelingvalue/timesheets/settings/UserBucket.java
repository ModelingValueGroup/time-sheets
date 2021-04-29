package nl.modelingvalue.timesheets.settings;

import java.util.regex.Pattern;

import de.micromata.jira.rest.core.domain.AuthorBean;
import nl.modelingvalue.timesheets.util.U;

public class UserBucket {
    public boolean ignore;
    public String  displayName;
    public long    budget;

    public String  name;
    private Pattern displayNamePat;

    public void init(String name) {
        this.name = name;
        if (displayName == null) {
            displayName = name;
        }
        if (displayNamePat == null) {
            displayNamePat = U.cachePattern(displayName);
        }
    }

    public boolean isMatch(AuthorBean ab) {
        if (ignore) {
            return false;
        }
        return displayNamePat.matcher(ab.getDisplayName()).matches() || displayNamePat.matcher(ab.getName()).matches();
    }

    public boolean isCatchAll() {
        return displayName.equals("/.*/");
    }
}
