package in.m.picks.poolold;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.shared.ConfigService;

public abstract class Pool {

	final Logger log = LoggerFactory.getLogger(Pool.class);

	protected ExecutorService executorService;
	protected volatile ArrayList<Future<?>> futures;
	private int poolSize;

	public Pool(String threadConfigKey) {
		poolSize = 4;
		try {
			poolSize = Integer.parseInt(ConfigService.INSTANCE.getConfig(threadConfigKey));
		} catch (NumberFormatException e) {
			log.warn(
					"Config [{}] : NumberFormatException {}. Using default value 4.",
					threadConfigKey, e.getLocalizedMessage());
		}
		start();
	}

	public void start() {
		executorService = Executors.newFixedThreadPool(poolSize);
		futures = new ArrayList<Future<?>>();
	}

	public void submit(Runnable task) {
		futures.add(executorService.submit(task));
	}

	public int getNotDone() {
		int count = 0;
		for (Future<?> future : futures) {
			if (!future.isDone()) {
				count++;
			}
		}
		return count;
	}

	public boolean isDone() {
		if (getNotDone() > 0) {
			return false;
		} else {
			return true;
		}
	}

	public void stop() {
		executorService.shutdown();
	}

	public boolean isTerminated() {
		return executorService.isTerminated();
	}
}
