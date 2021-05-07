package nl.modelingvalue.timesheets.model;

import java.util.List;

import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.info.PartInfo;
import nl.modelingvalue.timesheets.info.ProjectInfo;

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
        List<ProjectInfo> projectInfos = partInfo.allProjectInfosDeep().toList();
        return new TableModel(this, partInfo.id, projectInfos);
    }

    public List<TableModel> getSubTables() {
        return partInfo.allPartInfos()
                .filter(pi -> pi.notEmpty(year))
                .map(pi -> new TableModel(this, pi.id, pi.allProjectInfosDeep().toList()))
                .toList();
    }

    public String getRecalcUrl() {
        return Config.NOT_YET_IMPLEMENTED_URL;
    }

    public List<TableModel> getOtherProjects() {
        return getSubTables().stream().filter(tm -> !tm.parentModel.equals(this)).toList();
    }
}
