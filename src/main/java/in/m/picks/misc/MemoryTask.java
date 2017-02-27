package in.m.picks.misc;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.shared.MonitorService;

public class MemoryTask extends TimerTask {

	final Logger logger = LoggerFactory.getLogger(MemoryTask.class);

	@Override
	public void run() {
		Runtime runtime = Runtime.getRuntime();
		long mm = runtime.maxMemory();
		long fm = runtime.freeMemory();
		long tm = runtime.totalMemory();
		MonitorService.INSTANCE.pollMemory(mm, tm, fm);
		logger.debug("{} {} {} {}", new Date(), mm, tm, fm);
	}

}
