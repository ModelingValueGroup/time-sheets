package nl.modelingvalue.timesheets.info;

import java.util.regex.Pattern;

import de.micromata.jira.rest.core.domain.AccountBean;
import nl.modelingvalue.timesheets.SheetMaker;
import nl.modelingvalue.timesheets.util.U;

public class PersonInfo extends Info {
    public  String  fullName;
    public  String  regexp;
    //
    private Pattern regexpPat;

    public PersonInfo() {
    }

    public PersonInfo(AccountBean ab, SheetMaker sheetMaker) {
        // ab.getId() yields 'null' for AccountBeans
        id       = "UNKNOWN:" + ab.getName();
        index    = -1;
        fullName = "UNKNOWN:" + ab.getDisplayName();
        regexp   = ab.getDisplayName();
        init(sheetMaker);
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        if (fullName == null) {
            fullName = id;
        }
        if (regexp == null) {
            regexp = "/" + id + "/";
        }
        if (regexpPat == null) {
            regexpPat = U.cachePattern(regexp);
        }
    }

    public boolean isMatch(AccountBean ab) {
        return (ab.getDisplayName() != null && regexpPat.matcher(ab.getDisplayName()).find())
                || (ab.getName() != null && regexpPat.matcher(ab.getName()).find());
    }
}
