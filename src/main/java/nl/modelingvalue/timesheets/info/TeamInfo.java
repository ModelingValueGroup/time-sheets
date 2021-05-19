package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.SheetMaker;

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
        personRefs = persons.stream().map(sheetMaker::resolvePerson).toList();
    }

    public boolean isMember(PersonInfo person) {
        return personRefs.contains(person);
    }

    public Stream<PersonInfo> getTeamStream() {
        return personRefs.stream();
    }
}
