package nl.modelingvalue.timesheets.settings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class Settings {
    public static final String TIME_SHEETS_JSON       = "mvg-time-sheets.json";
    public static final String SETTINGS_CMD_LINE_FLAG = "-p";

    public static List<JiraBucket> read(String[] args) {
        Path f = getConnectFile(args);
        try {
            Type listType = new TypeToken<ArrayList<JiraBucket>>() {
            }.getType();
            Gson gson = new GsonBuilder()
                    //                .registerTypeAdapter(IssueBean.class, new IssueBeanDeserializer())
                    //                .registerTypeAdapter(MetaBean.class, new MetaBeanDeserializer())
                    .create();
            return gson.fromJson(new JsonReader(Files.newBufferedReader(f)), listType);
        } catch (IOException e) {
            throw new Error("can not read " + f.toAbsolutePath(), e);
        }
    }

    private static Path getConnectFile(String[] args) {
        Path found = Paths.get(TIME_SHEETS_JSON);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(SETTINGS_CMD_LINE_FLAG) && (i + 1) < args.length) {
                found = Paths.get(args[i + 1]);
                break;
            }
        }
        if (!Files.isRegularFile(found)) {
            throw new Error("file " + TIME_SHEETS_JSON + " not found (in current dir or as -p <f> in args)");
        }
        return found;
    }
}
