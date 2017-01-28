package in.m.picks.shared;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Activity;
import in.m.picks.model.Activity.Type;

public enum MonitorService {

	INSTANCE;

	final Logger logger = LoggerFactory.getLogger(MonitorService.class);

	List<Activity> activitesList;

	private MonitorService() {
		activitesList = new ArrayList<Activity>();
	}

	public void triggerFatal(String message){
		activitesList.add(new Activity(Type.FATAL, message));
		logActivities();
		logger.info("Picks Terminated");
		System.exit(1);
	}
	
	public void addActivity(Type type, String message) {
		activitesList.add(new Activity(type, message));
	}

	public void addActivity(Type type, String message, Throwable throwable) {
		activitesList.add(new Activity(type, message, throwable));
	}

	public void logActivities() {
		for (Activity activity : activitesList) {
			logger.info("{}", activity);
		}
	}

}
