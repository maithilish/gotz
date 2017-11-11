package org.codetab.gotz.shared;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.Appender.Marker;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.XFieldHelper;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppenderService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AppenderService.class);

    private final Map<String, Appender> appenders =
            new HashMap<String, Appender>();

    @Inject
    private DInjector dInjector;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    private XFieldHelper xFieldHelper;

    @Inject
    private AppenderService() {
    }

    public Appender getAppender(final String appenderName) {
        return appenders.get(appenderName);
    }

    public synchronized void createAppender(final String appenderName,
            final XField xField) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, XFieldException {
        String appenderClzName =
                xFieldHelper.getLastValue("//:appender/@class", xField);
        if (appenders.containsKey(appenderName)) {
            return;
        }

        Class<?> appenderClass = Class.forName(appenderClzName);
        Object obj = dInjector.instance(appenderClass);
        Appender appender = null;
        if (obj instanceof Appender) {
            appender = (Appender) obj;
        } else {
            throw new ClassCastException(
                    "Class " + appenderClzName + " is not of type Appender");
        }
        appender.setName(appenderName);
        appender.setXField(xField);
        appender.initializeQueue();
        appenderPoolService.submit("appender", appender);
        appenders.put(appenderName, appender);
    }

    public void closeAll() {
        for (String name : appenders.keySet()) {
            close(name);
        }
    }

    public void close(final String appenderName) {
        Appender appender = getAppender(appenderName);
        try {
            appender.append(Marker.EOF);
        } catch (InterruptedException e) {
            // don't throw else closeAll fails for other appenders
            LOGGER.warn("{}", Util.getMessage(e));
        }
    }
}
