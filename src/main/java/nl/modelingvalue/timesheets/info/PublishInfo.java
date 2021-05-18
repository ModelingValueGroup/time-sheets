package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;

import nl.modelingvalue.timesheets.SheetMaker;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldMayBeFinal"})
public class PublishInfo extends Info {
    public String          password;
    public List<String>    groups = new ArrayList<>();
    public List<GroupInfo> groupInfos;

    public PublishInfo() {
    }

    @Override
    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        groupInfos = groups.stream().map(sheetMaker::resolveGroup).toList();
    }

    public int indexOf(String name) {
        return groups.indexOf(name);
    }
}
