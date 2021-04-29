package nl.modelingvalue.timesheets.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.settings.RepoBucket;

@SuppressWarnings("unused")
public class YearModel extends Model<TimeAdminModel> {
    public final RepoBucket                repoBucket;
    public final int                       year;
    public final Map<String, ProjectModel> projectMap = new HashMap<>();

    public YearModel(TimeAdminModel timeAdminModel, RepoBucket repoBucket, int year) {
        super(timeAdminModel);
        this.repoBucket = repoBucket;
        this.year       = year;
    }

    public void add(WorkInfo wi) {
        getOrCreateSubModel(projectMap, wi.projectBucketName(), __ -> new ProjectModel(this, wi))
                .add(wi);
    }

    <T> Stream<T> selectFromAllWork(Function<WorkInfo, T> selector) {
        return projectMap.values().stream().flatMap(e -> e.selectFromAllWork(selector));
    }
}
