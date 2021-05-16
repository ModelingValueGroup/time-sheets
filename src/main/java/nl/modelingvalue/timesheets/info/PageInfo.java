package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.micromata.jira.rest.core.domain.ProjectBean;
import nl.modelingvalue.timesheets.SheetMaker;

public class PageInfo extends PartInfo {
    public  List<PartInfo> partInfos = new ArrayList<>();
    private boolean        accumulated;

    public PageInfo() {
    }

    public PageInfo(PartInfo partInfo) {
        super(partInfo);
    }

    public void init(SheetMaker sheetMaker) {
        super.init(sheetMaker);
        partInfos = parts.stream().map(sheetMaker::mustFindPart).toList();
    }

    public Stream<ProjectInfo> allProjectInfosDeep() {
        return partInfos.stream().flatMap(PartInfo::allProjectInfosDeep);
    }

    public Stream<PartInfo> allPartInfos() {
        return partInfos.stream();
    }

    public void matchPartsToProjects(List<ProjectBean> allProjectBeans) {
        List<ProjectBean> matching = allProjectBeans.stream().filter(pb -> pb.getKey().equals(id)).toList();
        if (!matching.isEmpty()) {
            throw new Error("config problem: the page " + id + " matches a project");
        }
    }

    public void accumulateSubs() {
        if (!accumulated) {
            accumulated = true;
            partInfos.forEach(pi -> {
                pi.accumulateSubs();
                yearPersonMonthInfo.add(pi);
            });
        }
    }
}
