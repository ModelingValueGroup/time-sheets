package nl.modelingvalue.timesheets.model;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;

public abstract class Model<P extends Model<?>> {
    public static final String            NOT_YET_IMPLEMENTED_URL = "http://www.bamu-gereedschappen.nl/wp-content/uploads/2018/09/under-construction.jpg";
    public static final long              BUDGET_PLACEHOLDER      = (long) (99.5 * 60 * 60); // TODO: sort out how to
    public static final DateTimeFormatter DATE_FORMATTER          = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
    public static final String[]          MONTH_NAMES             = {
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"
    };

    public final P parentModel;

    public Model(P parentModel) {
        this.parentModel = parentModel;
    }

    protected <K, M extends Model<?>> M getOrCreateSubModel(Map<K, M> map, K key, Function<K, M> modelCreator) {
        M m = map.get(key);
        if (m == null) {
            synchronized (this) {
                m = map.computeIfAbsent(key, modelCreator);
            }
        }
        return m;
    }

    public static String nbsp(String a) {
        return a.replaceAll(" ", "&nbsp;");
    }

    public static String hoursFromSec(long totalSec) {
        long   numQuarters = (totalSec * 4) / (60 * 60);
        double v           = numQuarters / 4.0;
        return String.format("%4.2f", v);
    }
}
