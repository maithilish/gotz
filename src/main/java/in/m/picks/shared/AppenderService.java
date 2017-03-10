package in.m.picks.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.appender.Appender;
import in.m.picks.appender.Appender.Marker;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.FieldsBase;
import in.m.picks.pool.AppenderPoolService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public enum AppenderService {

	INSTANCE;

	final Logger logger = LoggerFactory.getLogger(AppenderService.class);

	private final Map<String, Appender> appenders;

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

	public synchronized void createAppender(List<FieldsBase> fields)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, FieldNotFoundException {
		String appenderName = FieldsUtil.getValue(fields, "name");
		String appenderClzName = FieldsUtil.getValue(fields, "class");
		if (appenders.containsKey(appenderName)) {
			return;
		}
		Appender appender = null;
		Class<?> appenderClass = Class.forName(appenderClzName);
		Object obj = appenderClass.newInstance();
		if (obj instanceof Appender) {
			appender = (Appender) obj;
		} else {
			throw new ClassCastException(
					"Class " + appenderClzName + " is not of type Appender");
		}
		appender.setFields(fields);
		AppenderPoolService.getInstance().submit("appender", appender);
		appenders.put(appenderName, appender);
	}

	public void closeAll() {
		for (String name : appenders.keySet()) {
			try {
				close(name);
			} catch (InterruptedException e) {
				logger.warn("{}", Util.getMessage(e));
			}
		}
	}

	public void close(String appenderName) throws InterruptedException {
		Appender appender = getAppender(appenderName);
		appender.append(Marker.EOF);
	}
}
