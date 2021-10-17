package nl.modelingvalue.timesheets.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

import de.micromata.jira.rest.core.util.*;

public class Pool {
    public static final boolean         FORCE_SEQUENCIAL = Boolean.getBoolean("FORCE_SEQUENCIAL");
    public static final ExecutorService POOL             = Executors.newCachedThreadPool(new DaemonThreadFactory("POOL"));

    public static <T> void parallelExecAndWait(Stream<T> stream, Consumer<T> consumer) {
        if (FORCE_SEQUENCIAL) {
            stream.forEach(consumer);
        } else {
            stream.map(e -> POOL.submit(() -> consumer.accept(e)))
                    .toList() // this is essential to get all the futures in a list first!
                    .forEach(Pool::waitFor);
        }
    }

    public static <T> T waitFor(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new Wrapper(e);
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        private final String        poolName;
        private final ThreadGroup   group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        DaemonThreadFactory(String poolName) {
            this.poolName = poolName;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, String.format(poolName + "-%03d", threadNumber.getAndIncrement()), 0);
            t.setDaemon(true);
            return t;
        }
    }
}
