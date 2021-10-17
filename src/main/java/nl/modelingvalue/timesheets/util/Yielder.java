package nl.modelingvalue.timesheets.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import de.micromata.jira.rest.core.util.*;

/**
 * inspired by 'Producer' by Luke Hutchison (https://github.com/lukehutch/Producer)
 *
 * @param <T>
 */
@SuppressWarnings("unused")
public class Yielder<T> implements Iterable<T> {
    private static final int DEFAULT_QUEUE_SIZE = 100;

    private final YielderMethod<T>                yielderMethod;
    private final ExecutorService                 executor;
    private final ArrayBlockingQueue<Optional<T>> queue;
    private final Iterator<T>                     iterator;
    //
    private final AtomicBoolean                   yielderHasBeenStarted  = new AtomicBoolean(false);
    private final AtomicBoolean                   yielderHasBeenShutdown = new AtomicBoolean(false);
    //
    private final AtomicReference<Future<?>>      yielderFuture          = new AtomicReference<>();
    private final AtomicReference<Throwable>      producerException      = new AtomicReference<>();

    public static <T> Stream<T> stream(YielderMethod<T> yielderMethod) {
        return StreamSupport.stream(new Yielder<>(yielderMethod).spliterator(), true);
    }

    public static <T> Stream<T> stream(ExecutorService executor, YielderMethod<T> yielderMethod) {
        return StreamSupport.stream(new Yielder<>(executor, yielderMethod).spliterator(), false);
    }

    public static <T> Stream<T> stream(int queueSize, YielderMethod<T> yielderMethod) {
        return StreamSupport.stream(new Yielder<>(queueSize, yielderMethod).spliterator(), false);
    }

    public static <T> Stream<T> stream(int queueSize, ExecutorService executor, YielderMethod<T> yielderMethod) {
        return StreamSupport.stream(new Yielder<>(queueSize, executor, yielderMethod).spliterator(), false);
    }

    @FunctionalInterface
    public interface YielderMethod<T> {
        void produce(Yielder<T> yielder);
    }

    public Yielder(YielderMethod<T> yielderMethod) {
        this(DEFAULT_QUEUE_SIZE, yielderMethod);
    }

    public Yielder(ExecutorService executor, YielderMethod<T> yielderMethod) {
        this(DEFAULT_QUEUE_SIZE, executor, yielderMethod);
    }

    public Yielder(int queueSize, YielderMethod<T> yielderMethod) {
        this(queueSize, ForkJoinPool.commonPool(), yielderMethod);
    }

    public Yielder(int queueSize, ExecutorService executor, YielderMethod<T> yielderMethod) {
        this.yielderMethod = yielderMethod;
        this.executor      = executor;
        queue              = new ArrayBlockingQueue<>(queueSize);
        iterator           = new YieldIterator();
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    /**
     * Initiate immediate termination -- interrupt and shut down the yielder, and clear the queue.
     */
    public void shutdownYielder() {
        shutdownYielder(/* clearQueue = */ true);
    }

    /**
     * Yes, finalizers are broken -- but since the constructor starts a thread, the finalizer should ensure the
     * thread is torn down (and there's no other great way to ensure this right now on the JVM).
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutdownYielder();
    }

    public final void yieldz(T[] t) {
        yieldz(Arrays.stream(t));
    }

    public final void yieldz(Collection<T> c) {
        yieldz(c.stream());
    }

    public final void yieldz(Stream<T> s) {
        s.forEach(this::yieldz);
    }

    public final void yieldz(T t) {
        try {
            if (yielderHasBeenShutdown.get()) {
                throw new InterruptedException();
            }
            queue.put(Optional.of(t));
        } catch (InterruptedException e) {
            throw new Wrapper(e);
        }
    }

    private void startYielder() {
        yielderFuture.set(executor.submit(() -> {
            boolean terminatedPrematurely = true;
            try {
                try {
                    yielderMethod.produce(this);
                    queue.put(Optional.empty());
                    terminatedPrematurely = false;
                } catch (InterruptedException e1) {
                    // ignored
                } catch (RuntimeException e2) {
                    if (!(e2.getCause() instanceof InterruptedException)) {
                        producerException.set(e2);
                    }
                } catch (Exception e3) {
                    producerException.set(e3);
                }
            } finally {
                shutdownYielder(terminatedPrematurely);
            }
        }));
    }

    private void cancelYielder() {
        if (yielderFuture.get() != null && !yielderFuture.get().isDone()) {
            yielderFuture.get().cancel(true);
        }
    }

    private void shutdownYielder(boolean clearQueue) {
        if (!yielderHasBeenShutdown.getAndSet(true) && yielderHasBeenStarted.get()) {
            executor.submit(() -> {
                Future<?> future = yielderFuture.get();
                if (future != null) {
                    cancelYielder();
                    try {
                        // wait for yielder completion
                        future.get();
                    } catch (CancellationException | InterruptedException e) {
                        // Ignore
                    } catch (ExecutionException e) {
                        producerException.set(e.getCause());
                    }
                }
                if (clearQueue) {
                    queue.clear();
                    try {
                        queue.put(Optional.empty());
                    } catch (InterruptedException e) {
                        // Should not happen
                        throw new Wrapper("Could not push end-of-queue marker", e);
                    }
                }
            });
        }
    }

    private class YieldIterator implements Iterator<T> {
        private T       next;
        private boolean hasNext;
        private boolean ended;

        @Override
        public boolean hasNext() {
            getNext();
            return hasNext;
        }

        @Override
        public T next() {
            getNext();
            if (!hasNext) {
                throw new IllegalArgumentException("No next item");
            }
            hasNext = false;
            return next;
        }

        private void getNext() {
            if (!yielderHasBeenStarted.getAndSet(true) && !yielderHasBeenShutdown.get()) {
                startYielder();
            }
            if (!ended && !hasNext) {
                try {
                    queue.take().ifPresentOrElse(t -> {
                        next    = t;
                        hasNext = true;
                    }, () -> ended = true);
                } catch (InterruptedException e) {
                    // If the consumer is interrupted, cancel the yielder
                    cancelYielder();
                    // Re-set the interrupt status of the current thread
                    Thread.currentThread().interrupt();
                    // Re-throw as RuntimeException, since the iterator sequence
                    // will end earlier than expected, and the receiver should not
                    // assume the returned sequence is the full sequence
                    throw new Wrapper(e);
                }
            }
            // If producer threw an exception, re-throw it in the consumer
            Throwable t = producerException.get();
            if (t != null) {
                throw new Wrapper("Yielder threw an exception", t);
            }
        }
    }
}