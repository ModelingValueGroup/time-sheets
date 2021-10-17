package nl.modelingvalue.timesheets.model;

import java.util.*;
import java.util.function.*;

import nl.modelingvalue.timesheets.util.*;

public abstract class Model<P extends Model<?>> {
    public final P parentModel;

    public Model(P parentModel) {
        this.parentModel = parentModel;
    }

    public static String nbsp(String a) {
        return a.replaceAll(" ", "&nbsp;");
    }

    public static String hoursFromSecFormatted(long sec) {
        return sec == 0 ? "&nbsp;&nbsp;" : String.format("%4.2f", U.hoursFromSec(sec));
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
}
