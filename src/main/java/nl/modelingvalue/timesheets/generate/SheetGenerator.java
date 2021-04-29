package nl.modelingvalue.timesheets.generate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import nl.modelingvalue.timesheets.model.ProjectModel;
import nl.modelingvalue.timesheets.model.TimeAdminModel;
import nl.modelingvalue.timesheets.model.YearModel;
import nl.modelingvalue.timesheets.settings.Settings;
import nl.modelingvalue.timesheets.util.FreeMarkerEngine;

public class SheetGenerator {
    public static void generate(Settings settings, TimeAdminModel timeadmin, String fileNameTemplate) {
        System.err.println("generating to " + Paths.get(".").toAbsolutePath());
        settings.yearsMap.values().forEach(yearbucket -> {
            int          year         = yearbucket.year;
            YearModel    yearModel    = timeadmin.yearMap.get(year);
            List<String> projectNames = yearbucket.values().stream().map(pb -> pb.name).distinct().toList();
            projectNames.forEach(projectName -> {
                ProjectModel projectModel = yearModel.projectMap.get(projectName);
                if (projectModel != null) {
                    String page       = new FreeMarkerEngine().process("nl/modelingvalue/timesheets/timesheet.html.ftl", projectModel);
                    Path   outputFile = Paths.get(String.format(fileNameTemplate, year, projectName));
                    write(page, outputFile);
                    System.err.println("    " + outputFile.toAbsolutePath());
                }
            });
        });
    }

    private static void write(String page, Path outputFile) {
        try {
            Files.writeString(outputFile, page);
        } catch (IOException e) {
            throw new Error("can not write output file " + outputFile.toAbsolutePath(), e);
        }
    }
}
