package org.codetab.gotz.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public abstract class Pool {

    static final Logger LOGGER = LoggerFactory.getLogger(Pool.class);

    private static final int POOL_SIZE = 4;
    private static final int SLEEP_MILLIS = 1000;

    private Map<String, ExecutorService> executorsMap;
    private List<Future<?>> futures;

    @Inject
    private ConfigService configService;

    protected Pool() {
        executorsMap = new HashMap<String, ExecutorService>();
        futures = new ArrayList<Future<?>>();
    }

    private ExecutorService getPool(final String poolName) {
        ExecutorService executor = executorsMap.get(poolName);
        if (executor == null) {
            int poolSize = POOL_SIZE;
            String key = "gotz.poolsize." + poolName;
            try {
                String ps = configService.getConfig(key);
                poolSize = Integer.valueOf(ps);
            } catch (NumberFormatException | ConfigNotFoundException e) {
                LOGGER.warn(
                        "unable to get pool size for [{}], defaults to {}. {}",
                        key, POOL_SIZE, e);
            }
            executor = Executors.newFixedThreadPool(poolSize);
            LOGGER.info("create ExecutorPool [{}], pool size [{}]", poolName,
                    poolSize);
            executorsMap.put(poolName, executor);
        }
        return executor;
    }

    @GuardedBy("this")
    public synchronized boolean submit(final String poolName,
            final Runnable task) {
        ExecutorService pool = getPool(poolName);
        Future<?> future = pool.submit(task);
        return futures.add(future);
    }

    @GuardedBy("this")
    public final synchronized boolean isDone() {
        return getNotDone() == 0;
    }

    private long getNotDone() {
        futures.removeIf(Future::isDone);
        return futures.stream().count();
    }

    public final void shutdownAll() {
        executorsMap.values().stream().forEach(ExecutorService::shutdown);
    }

    public void waitForFinish() {
        while (!isDone()) {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.warn("wait for finish interrupted");
            }
        }
        shutdownAll();
        while (!isAllTerminated()) {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.warn("wait for finish interrupted");
            }
        }
    }

    private boolean isAllTerminated() {
        return executorsMap.values().stream()
                .allMatch(ExecutorService::isTerminated);
    }

}
