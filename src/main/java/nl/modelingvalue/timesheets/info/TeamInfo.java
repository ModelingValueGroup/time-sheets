package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.SheetMaker;
import nl.modelingvalue.timesheets.util.U;

public class TeamInfo extends Info {
    public List<String>     persons = new ArrayList<>();
    public List<PersonInfo> personRefs;

    public TeamInfo() {
    }

    public TeamInfo(String name, int index, List<String> persons) {
        super(name, index);
        this.persons = persons;
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        personRefs = persons.stream().map(name -> U.errorIfNull(sheetMaker.persons.get(name), "person", name)).toList();
    }

    public boolean isMember(PersonInfo person) {
        return personRefs.contains(person);
    }

    public Stream<PersonInfo> getTeamStream() {
        return personRefs.stream();
    }
}
