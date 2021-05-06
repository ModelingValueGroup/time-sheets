package nl.modelingvalue.timesheets.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.modelingvalue.timesheets.model.Model;

public class FreeMarker {
    public static void generate(String templateName, String outFileName, Model<?> projectModel) {
        String page       = new FreeMarkerEngine().process("nl/modelingvalue/timesheets/" + templateName + ".ftl", projectModel);
        Path   outputFile = Paths.get(outFileName);
        write(page, outputFile);
    }

    public static void write(String page, Path outputFile) {
        try {
            Files.writeString(outputFile, page);
        } catch (IOException e) {
            throw new Error("can not write output file " + outputFile.toAbsolutePath(), e);
        }
    }
}
