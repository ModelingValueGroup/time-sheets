package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;

import nl.modelingvalue.timesheets.util.U;

public class TeamInfo extends Info {
    public List<String>     persons = new ArrayList<>();
    public List<PersonInfo> personRefs;

    public void init(Settings settings) {
        super.init(settings);
        personRefs = persons.stream().map(name -> U.errorIfNull(settings.persons.get(name), "person", name)).toList();
    }
}
