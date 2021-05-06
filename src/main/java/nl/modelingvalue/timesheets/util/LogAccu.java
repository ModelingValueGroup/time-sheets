package nl.modelingvalue.timesheets.util;

import java.util.ArrayList;
import java.util.List;

public class LogAccu {
    public static final boolean TRACE    = Boolean.getBoolean("TRACE");
    public static final LogAccu INSTANCE = new LogAccu();

    public final List<String> log  = new ArrayList<>();
    public final List<String> info = new ArrayList<>();
    public final List<String> err  = new ArrayList<>();

    public static void log(String msg) {
        synchronized (INSTANCE.log) {
            INSTANCE.log.add(msg);
        }
        if (TRACE) {
            System.err.println("**LOG** " + msg);
        }
    }

    public static void info(String msg) {
        synchronized (INSTANCE.info) {
            INSTANCE.info.add(msg);
        }
        if (TRACE) {
            System.err.println("**INF** " + msg);
        }
    }

    public static void err(String msg) {
        synchronized (INSTANCE.err) {
            INSTANCE.err.add(msg);
        }
        if (TRACE) {
            System.err.println("**ERR** " + msg);
        }
    }
}
