package nl.modelingvalue.timesheets.generate;

import java.util.Map.Entry;

import nl.modelingvalue.timesheets.model.ProjectModel;
import nl.modelingvalue.timesheets.model.TimeAdminModel;
import nl.modelingvalue.timesheets.model.WorkInfo;
import nl.modelingvalue.timesheets.settings.Settings;
import nl.modelingvalue.timesheets.util.FreeMarkerEngine;

public class SheetGenerator {
    public static void generate(Settings settings, TimeAdminModel timeadmin) {
        //trace(timeadmin);

        ProjectModel projectModel = timeadmin.yearMap.get(2019).projectMap.get("dclare");

        String output = new FreeMarkerEngine().process("nl/modelingvalue/timesheets/timesheet.html.ftl", projectModel);
        System.out.println(output);
    }

    private static void trace(TimeAdminModel timeadmin) {
        timeadmin.yearMap.entrySet()
                .stream()
                .sorted(Entry.comparingByKey())
                .forEach(e -> {
                    System.err.println("+" + e.getKey()); // year
                    e.getValue().projectMap.entrySet()
                            .stream()
                            .sorted(Entry.comparingByKey())
                            .forEach(ee -> {
                                System.err.println("    =" + ee.getKey()); // project
                                ee.getValue().userMap.entrySet()
                                        .stream()
                                        .sorted(Entry.comparingByKey())
                                        .forEach(eee -> {
                                            System.err.println("        =" + eee.getKey()); // user
                                            eee.getValue().monthMap.entrySet()
                                                    .stream()
                                                    .sorted(Entry.comparingByKey())
                                                    .forEach(eeee -> {
                                                        long totalSec = eeee.getValue().workList.stream().mapToLong(WorkInfo::seconds).sum();
                                                        System.err.println("            #" + eeee.getKey() + "   => " + (totalSec / (60 * 60)) + " hours"); // month
                                                    });
                                        });
                            });
                });
    }
}
