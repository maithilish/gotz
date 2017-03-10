package in.m.picks.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.shared.ConfigService;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public abstract class Pool {

    static final Logger LOGGER = LoggerFactory.getLogger(Pool.class);

    private static final int POOL_SIZE = 4;
    private static final int SLEEP_MILLIS = 1000;

    private final Map<String, ExecutorService> executorsMap;
    private final ArrayList<Future<?>> futures;

    protected Pool() {
        executorsMap = new HashMap<String, ExecutorService>();
        futures = new ArrayList<Future<?>>();
    }

    private ExecutorService getPool(final String poolName) {
        ExecutorService executor = executorsMap.get(poolName);
        if (executor == null) {
            int poolSize = POOL_SIZE;
            try {
                String ps = ConfigService.INSTANCE
                        .getConfig("picks.poolsize." + poolName);
                poolSize = Integer.valueOf(ps);
            } catch (NumberFormatException e) {
            }
            executor = Executors.newFixedThreadPool(poolSize);
            LOGGER.info("create ExecutorPool [{}], pool size [{}]", poolName,
                    poolSize);
            executorsMap.put(poolName, executor);
        }
        return executor;
    }

    @GuardedBy("this")
    public final synchronized void submit(final String poolName,
            final Runnable task) {
        ExecutorService pool = getPool(poolName);
        Future<?> future = pool.submit(task);
        futures.add(future);
    }

    @GuardedBy("this")
    public final synchronized boolean isDone() {
        return getNotDone() == 0;
    }

    private int getNotDone() {
        int count = 0;
        for (Future<?> future : futures) {
            if (!future.isDone()) {
                count++;
            }
        }
        return count;
    }

    private boolean isAllTerminated() {
        for (ExecutorService pool : executorsMap.values()) {
            if (!pool.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    public final void shutdownAll() {
        for (ExecutorService pool : executorsMap.values()) {
            pool.shutdown();
        }
    }

    public final void waitForFinish() {
        while (!isDone()) {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        shutdownAll();
        while (!isAllTerminated()) {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
