package nl.modelingvalue.timesheets.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) typeToken.getRawType();

            // ... only HashMap<> based classes can be used as container:
            //noinspection rawtypes
            Class<? extends HashMap> mapBasedClass = findMapBasedClass(clazz);
            if (mapBasedClass == null) {
                return null;
            }

            // ... and it should be a parameterized class:
            Type genericSuperclass = mapBasedClass.getGenericSuperclass();
            if (!(genericSuperclass instanceof ParameterizedType genSuper)) {
                return null;
            }

            // ... with 2 types:
            Type[] types = genSuper.getActualTypeArguments();
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

            // ... and that class should have an id field of type String:
            Field idField = getField(valClazz, "id");
            if (idField == null || !idField.getType().equals(String.class)) {
                return null;
            }

            // ... and that class should have an index fields of type int:
            Field indexField = getField(valClazz, "index");
            if (indexField == null || !indexField.getType().equals(int.class)) {
                return null;
            }

            // now build us a TypeAdaptor that does some special handling:
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
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
                            Map<String, Object> x = (Map<String, Object>) clazz.getDeclaredConstructor().newInstance();
                            in.beginObject();
                            for (int i = 0; in.hasNext(); i++) {
                                String name = in.nextName();
                                Object y    = gson.getAdapter(valClazz).read(in);
                                x.put(name, y);
                                idField.set(y, name);
                                indexField.set(y, i);
                            }
                            in.endObject();

                            //noinspection unchecked
                            return (T) x;

                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };
        }

        @SuppressWarnings("rawtypes")
        private Class<? extends HashMap> findMapBasedClass(Class<?> clazz) {
            Class<?> c = clazz;
            while (c.getSuperclass() != null) {
                if (c.getSuperclass().equals(HashMap.class)) {
                    //noinspection unchecked
                    return (Class<? extends HashMap>) c;
                }
                c = c.getSuperclass();
            }
            return null;
        }

        private Field getField(Class<?> valClazz, String fieldName) {
            return Arrays.stream(valClazz.getDeclaredFields()).filter(f1 -> f1.getName().equals(fieldName)).findAny().map(f -> {
                f.setAccessible(true);
                return f;
            }).orElse(null);
        }
    }
}
