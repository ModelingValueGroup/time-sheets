package nl.modelingvalue.timesheets.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import de.micromata.jira.rest.core.util.*;
import nl.modelingvalue.timesheets.*;

public class LogAccu {
    public static final LogAccu INSTANCE = new LogAccu();

    public final List<String> err   = new ArrayList<>();
    public final List<String> info  = new ArrayList<>();
    public final List<String> trace = new ArrayList<>();
    public final List<String> debug = new ArrayList<>();

    public static void err(Throwable t) {
        Throwable tt = t;
        while (tt.getCause() != null && tt.getCause() != tt && (tt instanceof Wrapper || tt instanceof ExecutionException)) {
            tt = tt.getCause();
        }
        err("PROBLEM: " + tt.getClass().getSimpleName() + ": " + tt.getMessage());

        trace("");
        trace("Throwable detected:");
        StringWriter w = new StringWriter();
        try (PrintWriter pw = new PrintWriter(w)) {
            t.printStackTrace(pw);
        }
        Arrays.stream(w.toString().split("\n")).forEach(LogAccu::trace);
    }

    public static void err(String msg) {
        synchronized (INSTANCE.err) {
            INSTANCE.err.add(msg);
        }
        toStderr("ERR", msg);
    }

    public static void info(String msg) {
        synchronized (INSTANCE.info) {
            INSTANCE.info.add(msg);
        }
        toStderr("INF", msg);
    }

    public static void trace(String msg) {
        synchronized (INSTANCE.trace) {
            INSTANCE.trace.add(msg);
        }
        toStderr("TRC", msg);
    }

    public static void debug(String msg) {
        synchronized (INSTANCE.debug) {
            INSTANCE.debug.add(msg);
        }
        toStderr("DEB", msg);
    }

    private static void toStderr(String name, String msg) {
        if (Config.TRACE_TO_STDERR) {
            System.err.println("**" + name + "** " + msg);
        }
    }
}
