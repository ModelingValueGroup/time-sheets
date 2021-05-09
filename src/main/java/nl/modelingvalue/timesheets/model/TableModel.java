package nl.modelingvalue.timesheets.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;

import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.info.DetailInfo;
import nl.modelingvalue.timesheets.info.ProjectInfo;
import nl.modelingvalue.timesheets.util.Jql;
import nl.modelingvalue.timesheets.util.U;

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
            return Config.NOT_YET_IMPLEMENTED_URL;
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
                .sorted(Comparator.comparing(u -> u.id))
                .map(pi -> new UserModel(this, pi))
                .toList();
    }

    public String getProjectName() {
        return name;
    }

    public boolean hasBudget() {
        return 0 < getSecBudget();
    }

    private long getSec(ToLongFunction<DetailInfo> f) {
        return projectInfos.stream().mapToLong(pi -> pi.accountYearMonthInfo.secFor(parentModel.year, f)).sum();
    }

    private long getSecWorked() {
        return getSec(DetailInfo::secWorked);
    }

    private long getSecBudget() {
        return getSec(DetailInfo::secBudget);
    }

    private long getBudgetLeftSec() {
        return getSecBudget() - getSecWorked();
    }

    public String getWorked() {
        return U.hoursFromSecFormatted(getSecWorked());
    }

    public String getBudget() {
        return U.hoursFromSecFormatted(getSecBudget());
    }

    public String getBudgetLeft() {
        return U.hoursFromSecFormatted(getBudgetLeftSec());
    }

    public String getBudgetLeftClass() {
        List<String> classes = new ArrayList<>(List.of("budgetLeft"));
        if (getBudgetLeftSec() < 0) {
            classes.add("negative");
        }
        return String.join(" ", classes);
    }

    public String getUrl() {
        return Config.NOT_YET_IMPLEMENTED_URL;
    }

    public class MonthColumn {
        public final int    month;
        public final String name;

        public MonthColumn(int month) {
            this.month = month;
            this.name  = Config.MONTH_NAMES[month - 1];
        }

        public String getName() {
            return name;
        }

        private long getSec(ToLongFunction<DetailInfo> f) {
            return projectInfos.stream().mapToLong(pi -> pi.accountYearMonthInfo.secFor(parentModel.year, month, f)).sum();
        }

        private long getSecWorked() {
            return getSec(DetailInfo::secWorked);
        }

        private long getSecBudget() {
            return getSec(DetailInfo::secBudget);
        }

        public long getBudgetLeftSec() {
            return getSecBudget() - getSecWorked();
        }

        public long getBudgetLeftNowSec() {
            return getSecBudget() - getSecWorked();
        }

        public String getWorked() {
            return U.hoursFromSecFormatted(getSecWorked());
        }

        public String getBudget() {
            return U.hoursFromSecFormatted(getSecBudget());
        }

        public String getBudgetLeft() {
            return U.hoursFromSecFormatted(getBudgetLeftSec());
        }

        public String getBudgetLeftNow() {
            return U.hoursFromSecFormatted(getBudgetLeftNowSec());
        }

        public String getBudgetLeftClass() {
            List<String> classes = new ArrayList<>(List.of("budgetLeft"));
            if (getBudgetLeftSec() < 0) {
                classes.add("negative");
            }
            return String.join(" ", classes);
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
