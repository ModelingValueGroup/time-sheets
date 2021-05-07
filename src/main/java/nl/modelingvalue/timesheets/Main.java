package nl.modelingvalue.timesheets;

public class Main {
    public static void main(String[] args) {
        SheetMaker sheetMaker = SheetMaker.read(args);
        sheetMaker.connectAndAskProjects();
        sheetMaker.init();
        sheetMaker.matchPartsToProjects();
        sheetMaker.checkProjectConsistency();
        sheetMaker.downloadAllWorkItems();
        sheetMaker.generateAll();
    }
}
