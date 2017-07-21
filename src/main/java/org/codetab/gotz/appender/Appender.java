package org.codetab.gotz.appender;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Appender implements Runnable {

    static final Logger LOGGER = LoggerFactory.getLogger(Appender.class);

    public enum Marker {
        EOF
    }

    private String name;
    private List<FieldsBase> fields;
    private BlockingQueue<Object> queue;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private ConfigService configService;
    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ActivityService activityService;

    public abstract void append(Object object) throws InterruptedException;

    public void initializeQueue() {
        String queueSize = "4096";
        try {
            queueSize = configService.getConfig("gotz.appender.queuesize");
        } catch (ConfigNotFoundException e) {
        }
        try {
            queueSize = FieldsUtil.getValue(fields, "queuesize");
        } catch (FieldNotFoundException e) {
        }
        try {
            queue = new ArrayBlockingQueue<>(Integer.parseInt(queueSize));
            LOGGER.debug("created appender [{}] queue size [{}]", name,
                    queueSize);
        } catch (NumberFormatException e) {
            String message = Util.buildString(
                    "unable to create appender queue [", name, "]");
            LOGGER.error("{}, {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
        }
    }

    public BlockingQueue<Object> getQueue() {
        return queue;
    }

    /*
     *
     */
    public void setFields(final List<FieldsBase> fields) {
        this.fields = fields;
    }

    /*
     *
     */
    public List<FieldsBase> getFields() {
        return fields;
    }

    public void setName(final String appenderName) {
        this.name = appenderName;
    }

}
