package nl.modelingvalue.timesheets;

import static nl.modelingvalue.timesheets.util.LogAccu.err;
import static nl.modelingvalue.timesheets.util.LogAccu.trace;

public class Main {
    public static void main(String[] args) {
        trace("= TRACE_TO_STDERR   = " + Config.TRACE_TO_STDERR);
        trace("= TRACE_TO_HTML     = " + Config.TRACE_TO_HTML);
        trace("= CURRENT_YEAR_ONLY = " + Config.CURRENT_YEAR_ONLY);

        SheetMaker sheetMaker = SheetMaker.read(args);
        try {
            sheetMaker.init();
            sheetMaker.connectAndAskProjects();
            sheetMaker.resolveProjects();
            sheetMaker.checkProjectConsistency();
            sheetMaker.downloadAllWorkItems();
            sheetMaker.generateSupportFiles();
            sheetMaker.generateAll();
        } catch (Throwable t) {
            err(t);
            throw t;
        } finally {
            sheetMaker.generateSupportFiles();
            sheetMaker.generateIndex();
        }
    }
}
