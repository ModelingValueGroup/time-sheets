package nl.modelingvalue.timesheets.model;

import static nl.modelingvalue.timesheets.util.FreeMarkerEngine.NOW;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.util.Jql;

@SuppressWarnings("unused")
public class ProjectModel extends Model<YearModel> {
    public final String                 name;
    public final Map<String, UserModel> userMap = new HashMap<>();

    public ProjectModel(YearModel yearModel, WorkInfo wi) {
        super(yearModel);
        this.name = wi.projectBucketName();
    }

    public void add(WorkInfo wi) {
        getOrCreateSubModel(userMap, wi.userBucketName(), __ -> new UserModel(this, wi))
                .add(wi);
    }

    <T> Stream<T> selectFromAllWork(Function<WorkInfo, T> selector) {
        return userMap.values().stream().flatMap(e -> e.selectFromAllWork(selector));
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return String.format("%4d", parentModel.year);
    }

    public String getProjectName() {
        return name;
    }

    public String getNow() {
        return NOW.format(DateTimeFormatter.ofPattern("yyy-MM-dd_HH:mm:ss"));
    }

    public String getWriteTimeUrl() {
        List<String> projectKeys  = selectFromAllWork(w -> w.projectBean().getKey()).distinct().toList();
        List<String> statusValues = List.of("In Progress", "In Review");
        String       query        = Jql.and(Jql.in("project", projectKeys), Jql.in("status", statusValues));
        return parentModel.jiraBucket.url + "/issues/?jql=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    public List<UserModel> getUsers() {
        return userMap.values().stream().sorted(Comparator.comparing(u -> u.name)).toList();
    }

    public List<MonthColumn> getMonths() {
        return IntStream.rangeClosed(1, 12).mapToObj(month -> new MonthColumn(this, month)).toList();
    }

    private long getTotalSec() {
        return selectFromAllWork(WorkInfo::seconds).mapToLong(l -> l).sum();
    }

    public String getTotal() {
        return workHoursFromSec(getTotalSec());
    }

    private long getBudgetSec() {
        return BUDGET_PLACEHOLDER;
    }

    public String getBudget() {
        return workHoursFromSec(getBudgetSec());
    }

    private long getBudgetLeftSec() {
        return getBudgetSec() - getTotalSec();
    }

    public String getBudgetLeft() {
        return workHoursFromSec(getBudgetLeftSec());
    }

    public String getBudgetLeftClass() {
        List<String> classes = new ArrayList<>(List.of("budgetLeft"));
        if (getBudgetLeftSec() < 0) {
            classes.add("negative");
        }
        return String.join(" ", classes);
    }

    public String getRecalcUrl() {
        return NOT_YET_IMPLEMENTED_URL;
    }

    public List<ProjectModel> getOtherProjects() {
        return parentModel.parentModel.getAllProjects().stream().filter(p -> p != this && p.parentModel.year == parentModel.year).toList();
    }

    public String getUrl() {
        return NOT_YET_IMPLEMENTED_URL;
    }

    public static class MonthColumn {
        public final ProjectModel projectModel;
        public final int          month;
        public final String       name;

        public MonthColumn(ProjectModel projectModel, int month) {
            this.projectModel = projectModel;
            this.month        = month;
            this.name         = MONTH_NAMES[month - 1];
        }

        public String getName() {
            return name;
        }

        public long getTotalSec() {
            return projectModel.userMap.values().stream()
                    .mapToLong(u -> u.monthMap.getOrDefault(month, MonthModel.EMPTY).getWorkedSec())
                    .sum();
        }

        public long getBudgetSec() {
            return BUDGET_PLACEHOLDER;
        }

        public long getBudgetLeftSec() {
            return getBudgetSec() - getTotalSec();
        }

        public long getBudgetLeftNowSec() {
            return getBudgetSec() - getTotalSec();
        }

        public String getTotal() {
            return workHoursFromSec(getTotalSec());
        }

        public String getBudget() {
            return workHoursFromSec(getBudgetSec());
        }

        public String getBudgetLeft() {
            return workHoursFromSec(getBudgetLeftSec());
        }

        public String getBudgetLeftClass() {
            List<String> classes = new ArrayList<>(List.of("budgetLeft"));
            if (getBudgetLeftSec() < 0) {
                classes.add("negative");
            }
            return String.join(" ", classes);
        }

        public String getBudgetLeftNow() {
            return workHoursFromSec(getBudgetLeftNowSec());
        }

        public String getBudgetLeftNowClass() {
            List<String> classes = new ArrayList<>(List.of("budgetLeft"));
            if (getBudgetLeftNowSec() < 0) {
                classes.add("negative");
            }
            return String.join(" ", classes);
        }
    }
}
