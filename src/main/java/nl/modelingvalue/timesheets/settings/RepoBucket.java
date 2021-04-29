package nl.modelingvalue.timesheets.settings;

public final class RepoBucket {
    public boolean ignore;
    public String  url;
    public String  username;
    public String  apiToken;

    public String name;

    public void init(String name) {
        this.name = name;
    }
}
