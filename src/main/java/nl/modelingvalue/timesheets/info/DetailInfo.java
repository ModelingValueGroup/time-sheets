package nl.modelingvalue.timesheets.info;

import de.micromata.jira.rest.core.domain.WorkEntryBean;

public final class DetailInfo {
    public static final DetailInfo EMPTY = new DetailInfo();
    private             long       secWorked;
    private             long       secBudget;

    public long secWorked() {
        return secWorked;
    }

    public long secBudget() {
        return secBudget;
    }

    public void add(WorkEntryBean wb) {
        secWorked += wb.getTimeSpentSeconds();
    }
}
