package nl.modelingvalue.timesheets.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import nl.modelingvalue.timesheets.Config;
import nl.modelingvalue.timesheets.info.DetailInfo;
import nl.modelingvalue.timesheets.info.PGInfo;
import nl.modelingvalue.timesheets.info.ProjectInfo;
import nl.modelingvalue.timesheets.util.Jql;
import nl.modelingvalue.timesheets.util.U;

@SuppressWarnings("unused")
public class TableModel extends Model<PageModel> {
    public final PGInfo            pgInfo;
    public final List<MonthColumn> months;

    public TableModel(PageModel pageModel, PGInfo pgInfo, List<ProjectInfo> projectInfos) {
        super(pageModel);
        this.pgInfo = pgInfo;
        this.months = IntStream.rangeClosed(1, 12).mapToObj(MonthColumn::new).toList();
    }

    public String getName() {
        return pgInfo.id;
    }

    public String getYear() {
        return String.format("%4d", parentModel.year);
    }

    public List<MonthColumn> getMonths() {
        return months;
    }

    public String getWriteTimeUrl() {
        return pgInfo.serverUrlForAllProjects().map(url -> {
            List<String> projectKeys  = pgInfo.allProjectInfosDeep().filter(pi -> pi.getProjectBean() != null).map(pi -> pi.getProjectBean().getKey()).sorted().distinct().toList();
            List<String> statusValues = List.of("In Progress", "In Review");
            String       query        = Jql.and(Jql.in("project", projectKeys), Jql.in("status", statusValues));
            return url + "/issues/?jql=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        }).orElse(Config.UNDER_CONSTRUCTION_JPG);
    }

    public List<UserModel> getUsers() {
        return Stream.concat(
                pgInfo.yearPersonMonthInfo.getPersonInfoStream(parentModel.year),
                pgInfo.getTeamStream()
        )
                .distinct()
                .sorted()
                .map(pi -> new UserModel(this, pi))
                .toList();
    }

    public boolean hasBudget() {
        return pgInfo.yearPersonMonthInfo.hasBudget(parentModel.year);
    }

    private long getSec(ToLongFunction<DetailInfo> f) {
        return pgInfo.yearPersonMonthInfo.secFor(parentModel.year, f);
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
        return hoursFromSecFormatted(getSecWorked());
    }

    public String getBudget() {
        return hasBudget() ? hoursFromSecFormatted(getSecBudget()) : "";
    }

    public String getBudgetLeft() {
        return hasBudget() ? hoursFromSecFormatted(getBudgetLeftSec()) : "";
    }

    public String getBudgetLeftClass() {
        return U.jsClasses(getBudgetLeftSec(), "budgetLeft", "budget");
    }

    public String getUrl() {
        return Config.UNDER_CONSTRUCTION_JPG;
    }

    public String getPrevYear() {
        return null; //TODO future extension
    }

    public String getPrevYearUrl() {
        return null; //TODO future extension
    }

    public String getNextYear() {
        return null; //TODO future extension
    }

    public String getNextYearUrl() {
        return null; //TODO future extension
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
            return pgInfo.yearPersonMonthInfo.secFor(parentModel.year, month, f);
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

        public long getBudgetLeftCumulatedSec() {
            long workedCumulated = getMonths().subList(0, month).stream().mapToLong(MonthColumn::getSecWorked).sum();
            long budgetCumulated = getMonths().subList(0, month).stream().mapToLong(MonthColumn::getSecBudget).sum();
            return budgetCumulated - workedCumulated;
        }

        public String getWorked() {
            return hoursFromSecFormatted(getSecWorked());
        }

        public String getBudget() {
            return hasBudget() ? hoursFromSecFormatted(getSecBudget()) : "";
        }

        public String getBudgetLeft() {
            return hasBudget() ? hoursFromSecFormatted(getBudgetLeftSec()) : "";
        }

        public String getBudgetLeftCumulated() {
            return hasBudget() ? hoursFromSecFormatted(getBudgetLeftCumulatedSec()) : "";
        }

        public String getBudgetLeftClass() {
            return U.jsClasses(getBudgetLeftSec(), "budgetLeft", "budget");
        }

        public String getBudgetLeftCumulatedClass() {
            return U.jsClasses(getBudgetLeftCumulatedSec(), "budgetLeft", "budget");
        }
    }
}
