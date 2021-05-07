package nl.modelingvalue.timesheets.info;

import nl.modelingvalue.timesheets.SheetMaker;

public abstract class Info {
    public String     id;
    public int        index;
    public SheetMaker sheetMaker;

    public Info() {
    }

    public Info(PartInfo fromJson) {
        id         = fromJson.id;
        index      = fromJson.index;
        sheetMaker = fromJson.sheetMaker;
    }

    public void init(SheetMaker sheetMaker) {
        this.sheetMaker = sheetMaker;
    }
}
