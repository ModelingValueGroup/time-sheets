package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.SheetMaker;

public class GroupInfo extends PGInfo {
    public  List<String> groups   = new ArrayList<>();
    public  List<String> projects = new ArrayList<>();
    public  List<PGInfo> subInfos = new ArrayList<>();
    private boolean      accumulated;

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        if (projects.isEmpty() && groups.isEmpty()) {
            subInfos.addAll(sheetMaker.projects.values());
        } else {
            subInfos.addAll(groups.stream().map(sheetMaker::resolveGroup).toList());
            subInfos.addAll(projects.stream().map(sheetMaker::resolveProject).toList());
        }
    }

    public Stream<ProjectInfo> allProjectInfosDeep() {
        return subInfos.stream().flatMap(PGInfo::allProjectInfosDeep);
    }

    public Stream<PGInfo> allSubInfos() {
        return subInfos.stream();
    }

    public void accumulateSubs() {
        if (!accumulated) {
            accumulated = true;
            subInfos.forEach(pi -> {
                pi.accumulateSubs();
                yearPersonMonthInfo.add(pi);
            });
        }
    }
}
