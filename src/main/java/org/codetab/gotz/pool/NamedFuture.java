package org.codetab.gotz.pool;

import java.util.concurrent.Future;

public class NamedFuture {

    private String poolName;
    private Future<?> future;

    public NamedFuture(final String poolName, final Future<?> future) {
        this.poolName = poolName;
        this.future = future;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(final String poolName) {
        this.poolName = poolName;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(final Future<?> future) {
        this.future = future;
    }

    public boolean isDone() {
        return future.isDone();
    }
}
