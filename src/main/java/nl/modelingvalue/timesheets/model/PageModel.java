package nl.modelingvalue.timesheets.model;

import java.util.List;

import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.info.PGInfo;

public class PageModel extends Model<PageModel> {
    public final PGInfo pgInfo;
    public final int    year;

    public PageModel(PGInfo pgInfo, int year) {
        super(null);
        this.pgInfo = pgInfo;
        this.year   = year;
    }

    public String getStylesCss() {
        return Config.STYLES_CSS;
    }

    public String getScriptsJs() {
        return Config.SCRIPTS_JS;
    }

    public String getName() {
        return pgInfo.id;
    }

    public String getYear() {
        return String.format("%4d", year);
    }

    public TableModel getTotalTable() {
        return new TableModel(this, pgInfo, pgInfo.allProjectInfosDeep().toList());
    }

    public List<TableModel> getSubTables() {
        return pgInfo.allSubInfos()
                .filter(pi -> pi.notEmpty(year))
                .map(pi -> new TableModel(this, pi, pi.allProjectInfosDeep().toList()))
                .toList();
    }
}
