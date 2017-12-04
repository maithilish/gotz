package org.codetab.gotz.step.load;

import java.util.Collection;
import java.util.Map;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseAppender;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Append objects of type Data to defined appenders.
 * @author Maithilish
 *
 */
public class DataAppender extends BaseAppender {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(DataAppender.class);

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean process() {
        try {
            Map<String, Fields> appenderFieldsMap = getAppenderFieldsMap();
            for (String appenderName : appenderFieldsMap.keySet()) {
                Appender appender = getAppender(appenderName);
                Fields appenderFields = appenderFieldsMap.get(appenderName);
                Object encodedData = encode(appenderName, appenderFields);
                String stream = fieldsHelper.getValue(
                        "/xf:fields/xf:appender/@stream", appenderFields);
                if (encodedData instanceof Collection) {
                    Collection<?> list = (Collection<?>) encodedData;
                    if (stream.equalsIgnoreCase("false")) {
                        // bulk load
                        doAppend(appender, list);
                    } else {
                        // stream
                        for (Object obj : list) {
                            doAppend(appender, obj);
                        }
                    }
                } else {
                    doAppend(appender, encodedData);
                }
            }
        } catch (Exception e) {
            String message = "unable to find appender fields";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            throw new StepRunException(message, e);
        }
        return true;
    }
}
