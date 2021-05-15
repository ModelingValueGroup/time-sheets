package nl.modelingvalue.timesheets.model;

import java.util.List;

import nl.modelingvalue.timesheets.info.PartInfo;

public class PageModel extends Model<PageModel> {
    private final PartInfo partInfo;
    public final  int      year;

    public PageModel(PartInfo partInfo, int year) {
        super(null);
        this.partInfo = partInfo;
        this.year     = year;
    }

    public String getName() {
        return partInfo.id;
    }

    public String getYear() {
        return String.format("%4d", year);
    }

    public TableModel getTotalTable() {
        return new TableModel(this, partInfo, partInfo.allProjectInfosDeep().toList());
    }

    public List<TableModel> getSubTables() {
        return partInfo.allPartInfos()
                .filter(pi -> pi.notEmpty(year))
                .map(pi -> new TableModel(this, pi, pi.allProjectInfosDeep().toList()))
                .toList();
    }
}
