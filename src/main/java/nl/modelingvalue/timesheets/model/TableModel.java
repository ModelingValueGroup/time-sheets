package nl.modelingvalue.timesheets.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import nl.modelingvalue.timesheets.info.ProjectInfo;
import nl.modelingvalue.timesheets.util.Jql;

@SuppressWarnings("unused")
public class TableModel extends Model<PageModel> {
    public final String            name;
    public final List<ProjectInfo> projectInfos;

    public TableModel(PageModel pageModel, String name, List<ProjectInfo> projectInfos) {
        super(pageModel);
        this.name         = name;
        this.projectInfos = projectInfos;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return String.format("%4d", parentModel.year);
    }

    public String getWriteTimeUrl() {
        Optional<ProjectInfo> activeProjectOpt = projectInfos.stream().filter(pi -> pi.serverInfo != null).findFirst();
        if (activeProjectOpt.isEmpty()) {
            return NOT_YET_IMPLEMENTED_URL;
        } else {
            String       url          = activeProjectOpt.get().serverInfo.url;
            List<String> projectKeys  = projectInfos.stream().filter(pi -> pi.getProjectBean() != null).map(pi -> pi.getProjectBean().getKey()).toList();
            List<String> statusValues = List.of("In Progress", "In Review");
            String       query        = Jql.and(Jql.in("project", projectKeys), Jql.in("status", statusValues));
            return url + "/issues/?jql=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        }
    }

    public List<MonthColumn> getMonths() {
        return IntStream.rangeClosed(1, 12).mapToObj(MonthColumn::new).toList();
    }

    public List<UserModel> getUsers() {
        return projectInfos
                .stream()
                .flatMap(pi -> pi.accountYearMonthInfo.getPersonInfos().stream())
                .distinct()
                .map(pi -> new UserModel(this, pi))
                .toList();
    }

    public String getProjectName() {
        return name;
    }

    private long getTotalSec() {
        return projectInfos.stream().mapToLong(pi -> pi.accountYearMonthInfo.workSecFor(parentModel.year)).sum();
    }

    public String getTotal() {
        return hoursFromSec(getTotalSec());
    }

    private long getBudgetSec() {
        return BUDGET_PLACEHOLDER;
    }

    public String getBudget() {
        return hoursFromSec(getBudgetSec());
    }

    private long getBudgetLeftSec() {
        return getBudgetSec() - getTotalSec();
    }

    public String getBudgetLeft() {
        return hoursFromSec(getBudgetLeftSec());
    }

    public String getBudgetLeftClass() {
        List<String> classes = new ArrayList<>(List.of("budgetLeft"));
        if (getBudgetLeftSec() < 0) {
            classes.add("negative");
        }
        return String.join(" ", classes);
    }

    public String getUrl() {
        return NOT_YET_IMPLEMENTED_URL;
    }

    public class MonthColumn {
        public final int    month;
        public final String name;

        public MonthColumn(int month) {
            this.month = month;
            this.name  = MONTH_NAMES[month - 1];
        }

        public String getName() {
            return name;
        }

        public long getTotalSec() {
            return projectInfos.stream().mapToLong(pi -> pi.accountYearMonthInfo.workSecFor(parentModel.year, month)).sum();
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
            return hoursFromSec(getTotalSec());
        }

        public String getBudget() {
            return hoursFromSec(getBudgetSec());
        }

        public String getBudgetLeft() {
            return hoursFromSec(getBudgetLeftSec());
        }

        public String getBudgetLeftClass() {
            List<String> classes = new ArrayList<>(List.of("budgetLeft"));
            if (getBudgetLeftSec() < 0) {
                classes.add("negative");
            }
            return String.join(" ", classes);
        }

        public String getBudgetLeftNow() {
            return hoursFromSec(getBudgetLeftNowSec());
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
