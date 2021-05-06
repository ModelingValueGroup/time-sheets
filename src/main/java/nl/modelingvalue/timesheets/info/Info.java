package nl.modelingvalue.timesheets.info;

public class Info {
    public String   id;
    public int      index;
    public Settings settings;

    public void init(Settings settings) {
        this.settings = settings;
    }
}
