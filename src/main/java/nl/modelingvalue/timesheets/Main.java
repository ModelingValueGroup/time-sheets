package nl.modelingvalue.timesheets;

import java.util.List;

import nl.modelingvalue.timesheets.generate.SheetGenerator;
import nl.modelingvalue.timesheets.harvest.Harvester;
import nl.modelingvalue.timesheets.model.TimeAdminModel;
import nl.modelingvalue.timesheets.settings.JiraBucket;
import nl.modelingvalue.timesheets.settings.Settings;

public class Main {
    public static void main(String[] args) {
        List<JiraBucket> settings  = Settings.read(args);
        TimeAdminModel   timeadmin = new TimeAdminModel();

        Harvester.harvest(settings, timeadmin);
        SheetGenerator.generate(settings, timeadmin);
    }
}
