package in.m.picks.shared;

import java.util.HashMap;
import java.util.Map;

import in.m.picks.appender.Appender;
import in.m.picks.appender.Appender.Marker;
import in.m.picks.pool.AppenderPoolService;

public enum AppenderService {

	INSTANCE;

	final private Map<String, Appender> appenders;

	private AppenderService() {
		appenders = new HashMap<String, Appender>();
	}

	public synchronized Appender getAppender(String appenderName)
			throws InterruptedException {
		Appender appender = appenders.get(appenderName);
		if (appender == null) {
			throw new NullPointerException();
		}
		return appender;
	}

	public synchronized void createAppender(String name, String clzName)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		if (appenders.containsKey(name)) {
			return;
		}
		Appender appender = null;
		Class<?> appenderClass = Class.forName(clzName);
		Object obj = appenderClass.newInstance();
		if (obj instanceof Appender) {
			appender = (Appender) obj;
		} else {
			throw new ClassCastException(
					"Class " + clzName + " is not of type Appender");
		}
		AppenderPoolService.getInstance().submit("appender", appender);
		appenders.put(name, appender);
	}

	public void closeAll() {
		for (String name : appenders.keySet()) {
			try {
				close(name);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void close(String appenderName) throws InterruptedException {
		Appender appender = getAppender(appenderName);
		appender.append(Marker.EOF);
	}
}
