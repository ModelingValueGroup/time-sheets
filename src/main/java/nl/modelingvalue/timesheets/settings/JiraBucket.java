package nl.modelingvalue.timesheets.settings;

import java.util.List;

public class JiraBucket {
    public boolean             ignore;
    public String              name;
    public String              url;
    public String              username;
    public String              apiToken;
    public List<ProjectBucket> projectBuckets;
}
