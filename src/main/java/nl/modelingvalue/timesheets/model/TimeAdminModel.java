package nl.modelingvalue.timesheets.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class TimeAdminModel extends Model<TimeAdminModel> {
    public final Map<Integer, YearModel> yearMap = new HashMap<>();

    public TimeAdminModel() {
        super(null);
    }

    public void add(WorkInfo wi) {
        getOrCreateSubModel(yearMap, wi.year(), year -> new YearModel(this, wi.repoBucket(), year))
                .add(wi);
    }

    <T> Stream<T> selectFromAllWork(Function<WorkInfo, T> selector) {
        return yearMap
                .values()
                .stream()
                .flatMap(e -> e.selectFromAllWork(selector));
    }

    protected List<ProjectModel> getAllProjects() {
        return yearMap
                .values()
                .stream()
                .flatMap(y -> y.projectMap
                        .values()
                        .stream()
                )
                .sorted(Comparator.comparing(p -> p.name))
                .toList();
    }
}
