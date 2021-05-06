package nl.modelingvalue.timesheets;

import nl.modelingvalue.timesheets.info.Settings;

public class Main {
    public static void main(String[] args) {
        Settings settings = Settings.read(args);

        settings.connect();
        settings.downloadAllProjects();
        settings.checkProjectConsistency();
        settings.downloadAllWorkItems();
        settings.generateAll();
    }
}
