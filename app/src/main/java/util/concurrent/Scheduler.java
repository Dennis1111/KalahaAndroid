package util.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import util.concurrent.PausableThreadPoolExecutor;

/**
 * A thin wrapper around a thread pool executor that only exposes partially what the executor is
 * doing. This is so that we don't make a mistake somewhere along the way and jack something up.
 *
 * @author Matthew A. Johnston (warmwaffles)
 */
public class Scheduler {
    private PausableThreadPoolExecutor executor;
    private LinkedBlockingQueue<Runnable> queue;

    public Scheduler() {
        int processors = Runtime.getRuntime().availableProcessors();
        queue = new LinkedBlockingQueue<Runnable>();
        executor = new PausableThreadPoolExecutor(processors, 10, 10, TimeUnit.SECONDS, queue);
    }

    public void schedule(Runnable runnable) {
        executor.execute(runnable);
    }

    public void pause() {
        executor.pause();
    }

    public void resume() {
        executor.resume();
    }

    public void clear() {
        queue.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}