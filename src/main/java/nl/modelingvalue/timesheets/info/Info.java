package nl.modelingvalue.timesheets.info;

import nl.modelingvalue.timesheets.*;

public abstract class Info implements Comparable<Info> {
    public String     id;
    public int        index;
    public SheetMaker sheetMaker;

    public Info() {
    }

    public Info(String id, int index) {
        this.id    = id;
        this.index = index;
    }

    public void init(SheetMaker sheetMaker) {
        this.sheetMaker = sheetMaker;
    }

    @Override
    public int compareTo(Info o) {
        return index == o.index ? String.CASE_INSENSITIVE_ORDER.compare(id, o.id) : Integer.compare(index, o.index);
    }
}
