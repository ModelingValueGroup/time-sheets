package nl.modelingvalue.timesheets.util;

import static nl.modelingvalue.timesheets.Config.PUBLIC_DIRNAME;
import static nl.modelingvalue.timesheets.Config.RAW_DIRNAME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import nl.modelingvalue.timesheets.model.Model;

public class FreeMarker {
    public static void generate(String templateName, String outFileName, Model<?> projectModel, String password) {
        write(outFileName, password, new FreeMarkerEngine().process("nl/modelingvalue/timesheets/" + templateName + ".ftl", projectModel));
    }

    public static void write(String outputFile, String password, String page) {
        Path rawFile = Paths.get(RAW_DIRNAME, outputFile);
        Path pubFile = Paths.get(PUBLIC_DIRNAME, outputFile);
        try {
            Files.createDirectories(rawFile.getParent());
            Files.writeString(rawFile, page);
        } catch (IOException e) {
            throw new Error("can not write output file " + rawFile.toAbsolutePath(), e);
        }
        try {
            Files.createDirectories(pubFile.getParent());
            String wrapped = new PageEncryptWrapper(password).wrap(page);
            Files.writeString(pubFile, wrapped);
        } catch (IOException e) {
            throw new Error("can not write output file " + pubFile.toAbsolutePath(), e);
        }
    }
}
