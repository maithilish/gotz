package org.codetab.gotz.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.Appender.Marker;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppenderService {

    private final Logger logger = LoggerFactory.getLogger(AppenderService.class);

    private final Map<String, Appender> appenders;

    private DInjector dInjector;

    private AppenderPoolService appenderPoolService;

    @Inject
    public void setdInjector(DInjector dInjector) {
        this.dInjector = dInjector;
    }

    @Inject
    public void setAppenderPoolService(AppenderPoolService appenderPoolService) {
        this.appenderPoolService = appenderPoolService;
    }

    @Inject
    private AppenderService() {
        appenders = new HashMap<String, Appender>();
    }

    public synchronized Appender getAppender(final String appenderName)
            throws InterruptedException {
        Appender appender = appenders.get(appenderName);
        if (appender == null) {
            throw new NullPointerException();
        }
        return appender;
    }

    public synchronized void createAppender(final List<FieldsBase> fields)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            FieldNotFoundException {
        String appenderName = FieldsUtil.getValue(fields, "name");
        String appenderClzName = FieldsUtil.getValue(fields, "class");
        if (appenders.containsKey(appenderName)) {
            return;
        }
        Appender appender = null;
        Class<?> appenderClass = Class.forName(appenderClzName);
        Object obj = dInjector.instance(appenderClass);
        if (obj instanceof Appender) {
            appender = (Appender) obj;
        } else {
            throw new ClassCastException(
                    "Class " + appenderClzName + " is not of type Appender");
        }
        appender.setFields(fields);
        appenderPoolService.submit("appender", appender);
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

    public void close(final String appenderName) throws InterruptedException {
        Appender appender = getAppender(appenderName);
        appender.append(Marker.EOF);
    }
}
