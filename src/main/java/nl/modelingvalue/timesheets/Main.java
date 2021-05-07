package nl.modelingvalue.timesheets;

import nl.modelingvalue.timesheets.util.LogAccu;

public class Main {
    public static void main(String[] args) {
        LogAccu.info("running with CURRENT_YEAR_ONLY=" + Config.CURRENT_YEAR_ONLY);

        SheetMaker sheetMaker = SheetMaker.read(args);
        sheetMaker.connectAndAskProjects();
        sheetMaker.init();
        sheetMaker.matchPartsToProjects();
        sheetMaker.checkProjectConsistency();
        sheetMaker.downloadAllWorkItems();
        sheetMaker.generateAll();
    }
}
