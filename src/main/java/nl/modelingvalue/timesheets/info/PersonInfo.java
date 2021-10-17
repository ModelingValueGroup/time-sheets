package nl.modelingvalue.timesheets.info;

import java.util.*;
import java.util.regex.*;

import de.micromata.jira.rest.core.domain.*;
import nl.modelingvalue.timesheets.*;
import nl.modelingvalue.timesheets.util.*;

public class PersonInfo extends Info {
    public       boolean                 ignore;
    public       boolean                 retired;
    public       String                  fullName;
    public       String                  regexp;
    public final Map<ServerInfo, String> accountIdMap = new HashMap<>();
    //
    private      Pattern                 regexpPat;

    public PersonInfo() {
    }

    public PersonInfo(ServerInfo serverInfo, AccountBean ab, SheetMaker sheetMaker) {
        // NB: ab.getId() yields 'null' for AccountBeans
        id       = "UNKNOWN:" + ab.getName();
        index    = -1;
        fullName = "UNKNOWN:" + ab.getDisplayName();
        regexp   = ab.getDisplayName();
        connectAccount(serverInfo, ab);
        init(sheetMaker);
    }

    public void connectAccount(ServerInfo serverInfo, AccountBean ab) {
        String accountId = ab.getAccountId();
        if (accountId != null) {
            String prev = accountIdMap.get(serverInfo);
            if (!accountId.equals(prev)) {
                accountIdMap.put(serverInfo, accountId);
                if (prev != null) {
                    LogAccu.err("user has two ids (" + prev + "!=" + accountId + ") for " + ab + " at " + serverInfo);
                }
            }
        }
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        if (fullName == null) {
            fullName = id;
        }
        if (regexp == null) {
            regexp = "/" + Pattern.quote(id) + "/";
        }
        if (regexpPat == null) {
            regexpPat = U.cachePattern(regexp);
        }
    }

    public boolean isMatch(AccountBean ab) {
        boolean b = (ab.getDisplayName() != null && regexpPat.matcher(ab.getDisplayName()).find())
                    || (ab.getName() != null && regexpPat.matcher(ab.getName()).find());
        if (fullName.contains("Har")) {
            int x = 10;
        }
        return b;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
