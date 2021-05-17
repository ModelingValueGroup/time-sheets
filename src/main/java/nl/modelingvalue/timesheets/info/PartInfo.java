package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.micromata.jira.rest.core.domain.ProjectBean;
import nl.modelingvalue.timesheets.SheetMaker;

@SuppressWarnings("FieldMayBeFinal")
public class PartInfo extends Info {
    public List<String>                parts   = new ArrayList<>();
    public Map<String, YearBudgetInfo> budgets = new HashMap<>();
    //
    public YearPersonMonthInfo         yearPersonMonthInfo;

    public PartInfo() {
    }

    public PartInfo(PartInfo fromJson) {
        super(fromJson);
        parts               = fromJson.parts;
        budgets             = fromJson.budgets;
        yearPersonMonthInfo = new YearPersonMonthInfo(this);
    }

    public PartInfo makeActualPart() {
        return parts.isEmpty() ? new ProjectInfo(this) : new PageInfo(this);
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        budgets.values().forEach(v -> v.init(sheetMaker));
        yearPersonMonthInfo.init(sheetMaker);
        yearPersonMonthInfo.add(budgets.values());
    }

    public Stream<Integer> getYears() {
        return yearPersonMonthInfo.getYears();
    }

    public Stream<ProjectInfo> allProjectInfosDeep() {
        throw new Error("should have been implemented in superclass");
    }

    public Stream<PartInfo> allPartInfos() {
        throw new Error("should have been implemented in superclass");
    }

    public void matchPartsToProjects(List<ProjectBean> allProjectBeans) {
        throw new Error("should have been implemented in superclass");
    }

    public void accumulateSubs() {
    }

    public boolean notEmpty(int year) {
        return yearPersonMonthInfo.notEmpty(year);
    }
}
