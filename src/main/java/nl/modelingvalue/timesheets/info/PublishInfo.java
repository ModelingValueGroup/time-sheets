package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;

import nl.modelingvalue.timesheets.SheetMaker;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldMayBeFinal"})
public class PublishInfo extends Info {
    public String         password;
    public List<String>   parts = new ArrayList<>();
    public List<PartInfo> partInfos;

    public PublishInfo() {
    }

    @Override
    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        partInfos = parts.stream().map(sheetMaker::mustFindPart).toList();
    }
}
