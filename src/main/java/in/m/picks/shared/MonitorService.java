package in.m.picks.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.misc.MemoryTask;
import in.m.picks.model.Activity;
import in.m.picks.model.Activity.Type;

public enum MonitorService {

	INSTANCE;

	final Logger logger = LoggerFactory.getLogger(MonitorService.class);

	List<Activity> activitesList;

	Map<String, Long> memoryHighs = new HashMap<>();
	Map<String, Long> memoryLows = new HashMap<>();

	private Timer timer;
	private StopWatch stopWatch;

	private MonitorService() {
		activitesList = new ArrayList<Activity>();
	}

	public void start() {
		stopWatch = new StopWatch();
		stopWatch.start();
		timer = new Timer("Memory Timer");
		timer.schedule(new MemoryTask(), 0, 5000);
	}

	public void triggerFatal(String message) {
		activitesList.add(new Activity(Type.FATAL, message));
		end();
		logger.info("Picks Terminated");
		System.exit(1);
	}

	public void addActivity(Type type, String message) {
		activitesList.add(new Activity(type, message));
	}

	public void addActivity(Type type, String message, Throwable throwable) {
		activitesList.add(new Activity(type, message, throwable));
	}

	public void end() {
		logger.info("{}", "Picks run completed");
		timer.cancel();
		logActivities();
		logMemoryUsage();
		stopWatch.stop();
		logger.info("{}  {}", "Total time:", stopWatch);
	}

	private void logMemoryUsage() {
		logger.info("{}", "--- Memory Usage ---");
		logger.info("Highs: {}", memoryUsage(memoryHighs));
		logger.info("Lows: {}", memoryUsage(memoryLows));
	}

	private String memoryUsage(Map<String, Long> map) {
		StringBuilder sb = new StringBuilder();
		for (String name : map.keySet()) {
			Long inMB = (map.get(name)) / (1024 * 1024);
			sb.append(name);
			sb.append(" : ");
			sb.append(inMB);
			sb.append("M   ");
		}
		return sb.toString();
	}

	private void logActivities() {
		logger.info("{}", "--- Summary ---");
		if(activitesList.size() == 0){
			logger.info("no issues");
		}
		for (Activity activity : activitesList) {
			logger.info("Activity type={}", activity.getType());
			logger.info("         message={}", activity.getMessage());
			logger.info("         {}={}",
					activity.getThrowable().getClass().getSimpleName(),
					activity.getThrowable().getLocalizedMessage());
		}
	}

	public void pollMemory(Long maxMemory, Long totalMemory, Long freeMemory) {
		setHighs("Total", totalMemory);
		setHighs("Maximum", maxMemory);
		setHighs("Free", freeMemory);
		setLows("Total", totalMemory);
		setLows("Maximum", maxMemory);
		setLows("Free", freeMemory);
	}

	private void setHighs(String name, long value) {
		Long previousValue = memoryHighs.get(name);
		if (previousValue == null) {
			memoryHighs.put(name, value);
		} else {
			if (value > previousValue) {
				memoryHighs.put(name, value);
			}
		}
	}

	private void setLows(String name, long value) {
		Long previousValue = memoryLows.get(name);
		if (previousValue == null) {
			memoryLows.put(name, value);
		} else {
			if (value < previousValue) {
				memoryLows.put(name, value);
			}
		}
	}

}
