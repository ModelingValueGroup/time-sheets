package nl.modelingvalue.timesheets.info;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.util.U;

public class PageInfo extends Info {
    public String            password;
    public List<String>      projectNames = new ArrayList<>();
    public List<String>      pageNames    = new ArrayList<>();
    //
    public List<ProjectInfo> projectInfos;
    public List<PageInfo>    pageInfos;

    public void init(Settings settings) {
        super.init(settings);
        projectInfos = projectNames.stream().map(name -> U.errorIfNull(settings.projects.get(name), "project", name)).toList();
        pageInfos    = pageNames.stream().map(name -> U.errorIfNull(settings.pages.get(name), "page", name)).toList();
    }

    public Stream<Integer> getYears() {
        return projectInfos.stream().flatMap(ProjectInfo::getYears).distinct();
    }

    public Stream<ProjectInfo> allProjectInfosDeep() {
        return Stream.concat(projectInfos.stream(), pageInfos.stream().flatMap(PageInfo::allProjectInfosDeep)).distinct();
    }

    public boolean notEmpty(int year) {
        return allProjectInfosDeep().anyMatch(pi -> pi.accountYearMonthInfo.notEmpty(year));
    }
}
