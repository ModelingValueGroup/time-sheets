package nl.modelingvalue.timesheets.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * This is an implementation of a workaround for deserialisation of java 16 records by gson.
 * see: https://github.com/google/gson/issues/1794
 */
public class GsonUtils {
    public static Gson plain() {
        return new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .create();
    }

    public static Gson withSpecials() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                .registerTypeAdapterFactory(new OrderAdapterFactory())
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .create();
    }

    public static class RecordTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            TypeAdapter<T> typeAdapter = null;
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) type.getRawType();
            if (clazz.isRecord()) {
                TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

                typeAdapter = new TypeAdapter<>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        delegate.write(out, value);
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        } else {
                            var recordComponents = clazz.getRecordComponents();
                            var typeMap          = new HashMap<String, Class<?>>();
                            for (RecordComponent recordComponent : recordComponents) {
                                typeMap.put(recordComponent.getName(), recordComponent.getType());
                            }
                            var argsMap = new HashMap<String, Object>();
                            in.beginObject();
                            while (in.hasNext()) {
                                String name = in.nextName();
                                argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(in));
                            }
                            in.endObject();

                            var argTypes = new Class<?>[recordComponents.length];
                            var args     = new Object[recordComponents.length];
                            for (int i = 0; i < recordComponents.length; i++) {
                                argTypes[i] = recordComponents[i].getType();
                                args[i]     = argsMap.get(recordComponents[i].getName());
                            }
                            Constructor<T> constructor;
                            try {
                                constructor = clazz.getDeclaredConstructor(argTypes);
                                constructor.setAccessible(true);
                                return constructor.newInstance(args);
                            } catch (NoSuchMethodException | InstantiationException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                };
            }
            return typeAdapter;
        }
    }

    private static class OrderAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            ParameterizedType genMapType = findMapType(typeToken);

            if (genMapType == null) {
                return null;
            }

            // ... with 2 type args:
            Type[] types = genMapType.getActualTypeArguments();
            if (types.length != 2) {
                return null;
            }

            // ... the first being String:
            Type keyType = types[0];
            if (!keyType.equals(String.class)) {
                return null;
            }

            // ... the second being some class:
            Type valType = types[1];
            if (!(valType instanceof Class<?> valClazz)) {
                return null;
            }

            // ... and that class should have either an id field of type String or an index field of type int:
            Field idField    = getField("id", String.class, valClazz);
            Field indexField = getField("index", int.class, valClazz);
            if (idField == null && indexField == null) {
                return null;
            }

            // now build us a TypeAdaptor that does some special handling:
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
            Supplier<Constructor<?>> constructor = () -> {
                try {
                    Class<? super T> rawType = typeToken.getRawType();
                    if (rawType.equals(Map.class)) {
                        return HashMap.class.getConstructor();
                    } else {
                        return rawType.getConstructor();
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };

            return new TypeAdapter<>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    } else {
                        try {
                            //noinspection unchecked
                            Map<String, Object> x = (Map<String, Object>) constructor.get().newInstance();
                            in.beginObject();
                            for (int i = 0; in.hasNext(); i++) {
                                String name = in.nextName();
                                Object y    = gson.getAdapter(valClazz).read(in);
                                x.put(name, y);
                                if (idField != null) {
                                    idField.set(y, name);
                                }
                                if (indexField != null) {
                                    indexField.set(y, i);
                                }
                            }
                            in.endObject();

                            //noinspection unchecked
                            return (T) x;

                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };
        }

        private Field getField(String name, Class<?> t, Class<?> c) {
            Optional<Field> fieldOpt = getAllFields(c, null).stream().filter(f -> f.getName().equals(name) && f.getType().equals(t)).findAny();
            fieldOpt.ifPresent(f -> f.setAccessible(true));
            return fieldOpt.orElse(null);
        }

        private static List<Field> getAllFields(Class<?> c, List<Field> fields) {
            if (fields == null) {
                fields = new ArrayList<>();
            }
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            if (c.getSuperclass() != null) {
                getAllFields(c.getSuperclass(), fields);
            }
            return fields;
        }

        @SuppressWarnings("rawtypes")
        private ParameterizedType findMapType(TypeToken typeToken) {
            Class c = typeToken.getRawType();
            if (Map.class.isAssignableFrom(c)) {
                Type t = typeToken.getType();
                if (t instanceof ParameterizedType) {
                    return (ParameterizedType) t;
                }
            }
            Type genericSuperclass = c.getGenericSuperclass();
            if (genericSuperclass == null) {
                return null;
            }
            return findMapType(TypeToken.get(genericSuperclass));
        }

    }
}
