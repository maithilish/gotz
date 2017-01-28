package in.m.picks.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public abstract class Pool {

	final protected Map<String, ExecutorService> executorsMap;
	final protected ArrayList<Future<?>> futures;
	final private int poolSize;

	protected Pool() {
		poolSize = 4;
		executorsMap = new HashMap<String, ExecutorService>();
		futures = new ArrayList<Future<?>>();
	}

	private ExecutorService getPool(String poolName) {
		ExecutorService executor = executorsMap.get(poolName);
		if (executor == null) {
			executor = Executors.newFixedThreadPool(poolSize);
			executorsMap.put(poolName, executor);
		}
		return executor;
	}

	@GuardedBy("this")
	public synchronized void submit(String poolName, Runnable task) {
		ExecutorService pool = getPool(poolName);
		Future<?> future = pool.submit(task);
		futures.add(future);
	}

	@GuardedBy("this")
	public synchronized boolean isDone() {
		if (getNotDone() == 0) {
			return true;
		} else {
			return false;

		}
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

	public void shutdownAll() {
		for (ExecutorService pool : executorsMap.values()) {
			pool.shutdown();
		}
	}

	public void waitForFinish() {
		while (!isDone()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		shutdownAll();
		while (!isAllTerminated()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
