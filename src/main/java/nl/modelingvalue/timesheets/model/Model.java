package nl.modelingvalue.timesheets.model;

import java.util.Map;
import java.util.function.Function;

public abstract class Model<P extends Model<?>> {
    public final P parentModel;

    public Model(P parentModel) {
        this.parentModel = parentModel;
    }

    public static String nbsp(String a) {
        return a.replaceAll(" ", "&nbsp;");
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
