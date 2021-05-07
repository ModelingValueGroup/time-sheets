package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;

import nl.modelingvalue.timesheets.SheetMaker;
import nl.modelingvalue.timesheets.util.U;

public class TeamInfo extends Info {
    public List<String>     persons = new ArrayList<>();
    public List<PersonInfo> personRefs;

    public TeamInfo() {
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        personRefs = persons.stream().map(name -> U.errorIfNull(sheetMaker.persons.get(name), "person", name)).toList();
    }
}
