package nl.modelingvalue.timesheets;

import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.LogAccu.info;

public class Main {
    public static void main(String[] args) {
        info("= TRACE_TO_STDERR   = " + Config.TRACE_TO_STDERR);
        info("= TRACE_TO_HTML     = " + Config.TRACE_TO_HTML);
        info("= CURRENT_YEAR_ONLY = " + Config.CURRENT_YEAR_ONLY);

        SheetMaker sheetMaker = SheetMaker.read(args);
        try {
            sheetMaker.connectAndAskProjects();
            sheetMaker.init();
            sheetMaker.matchPartsToProjects();
            sheetMaker.checkProjectConsistency();
            sheetMaker.downloadAllWorkItems();
            sheetMaker.generateAll();
        } catch (Throwable t) {
            err(t);
            throw t;
        } finally {
            sheetMaker.generateIndex();
        }
    }
}
