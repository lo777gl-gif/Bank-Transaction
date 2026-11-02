import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Runs producers on a fixed thread pool and shuts it down gracefully: no new tasks are accepted, running
 * ones finish, then every Future is checked so a failed producer is never silent. Because delivery is
 * synchronous, when run() returns every published event has been fully processed by every listener.
 */
public class Simulation {
    private final int threads;
    private final long timeoutSeconds;

    public Simulation(int threads, long timeoutSeconds) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        this.threads = threads;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void run(List<? extends Runnable> producers) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (Runnable producer : producers) {
                futures.add(pool.submit(producer));
            }
            pool.shutdown();
            if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                throw new IllegalStateException("producers did not finish within " + timeoutSeconds + " s");
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException("a producer failed", e.getCause());
        } finally {
            pool.shutdownNow();   // no-op after a clean finish; stops stragglers on timeout or interrupt
        }
    }
}
