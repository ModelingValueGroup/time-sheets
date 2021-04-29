package nl.modelingvalue.timesheets.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;

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
    public static Gson withoutRecords() {
        return new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .create();
    }

    public static Gson withRecords() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
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
                    public T read(JsonReader reader) throws IOException {
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                            return null;
                        } else {
                            var recordComponents = clazz.getRecordComponents();
                            var typeMap          = new HashMap<String, Class<?>>();
                            for (RecordComponent recordComponent : recordComponents) {
                                typeMap.put(recordComponent.getName(), recordComponent.getType());
                            }
                            var argsMap = new HashMap<String, Object>();
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String name = reader.nextName();
                                argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(reader));
                            }
                            reader.endObject();

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
}
