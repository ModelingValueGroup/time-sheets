package nl.modelingvalue.timesheets.model;

import java.util.List;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.info.PageInfo;
import nl.modelingvalue.timesheets.info.ProjectInfo;

public class PageModel extends Model<PageModel> {
    private final PageInfo pageInfo;
    public final  int      year;

    public PageModel(PageInfo pageInfo, int year) {
        super(null);
        this.pageInfo = pageInfo;
        this.year     = year;
    }

    public String getName() {
        return pageInfo.id;
    }

    public int getYear() {
        return year;
    }

    public TableModel getTotalTable() {
        List<ProjectInfo> projectInfos = pageInfo.allProjectInfosDeep().toList();
        return new TableModel(this, pageInfo.id, projectInfos);
    }

    public List<TableModel> getSubTables() {
        return Stream.concat(
                pageInfo.pageInfos.stream().filter(pi->pi.notEmpty(year)).map(pi -> new TableModel(this, pi.id, pi.allProjectInfosDeep().toList())),
                pageInfo.projectInfos.stream().filter(pi -> pi.accountYearMonthInfo.notEmpty(year)).map(pi -> new TableModel(this, pi.id, List.of(pi)))
        ).toList();
    }

    public String getRecalcUrl() {
        return NOT_YET_IMPLEMENTED_URL;
    }

    public List<TableModel> getOtherProjects() {
        return getSubTables().stream().filter(tm -> !tm.equals(this)).toList();
    }
}
