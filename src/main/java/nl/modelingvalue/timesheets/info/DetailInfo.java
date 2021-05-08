package nl.modelingvalue.timesheets.info;

import nl.modelingvalue.timesheets.util.LogAccu;
import nl.modelingvalue.timesheets.util.U;

public final class DetailInfo {
    public static final DetailInfo EMPTY_DETAIL = new DetailInfo();
    private             long       secWorked;
    private             long       secBudget;

    public DetailInfo() {
    }

    public DetailInfo(long secWorked, long secBudget) {
        this.secWorked = secWorked;
        this.secBudget = secBudget;
    }

    public long secWorked() {
        return secWorked;
    }

    public long secBudget() {
        return secBudget;
    }

    public void add(DetailInfo detail) {
        secWorked += detail.secWorked;
        secBudget += detail.secBudget;
    }
}
