package org.codetab.gotz.step.load.appender;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Appender Task.
 * @author Maithilish
 *
 */
public abstract class Appender implements Runnable {

    /**
     * <p>
     * Marker constants for appenders.
     * @author Maithilish
     *
     */
    public enum Marker {
        /**
         * End of input.
         */
        EOF
    }

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Appender.class);

    /**
     * Appender name.
     */
    private String name;
    /**
     * Appender fields.
     */
    private Fields fields;
    /**
     * Queue to hold objects pushed to appenders.
     */
    private BlockingQueue<Object> queue;

    /**
     * Config service.
     */
    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private ConfigService configService;

    /**
     * Activity service.
     */
    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ActivityService activityService;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected FieldsHelper fieldsHelper;

    /**
     * <p>
     * Abstract append method.
     * @param object
     *            object to append
     * @throws InterruptedException
     *             on interruption.
     */
    public abstract void append(Object object) throws InterruptedException;

    /**
     * <p>
     * Initializes the blocking queue which is used to hold the objected pushed
     * to appender. By default, queue size is 4096 and it is configurable
     * globally with gotz.appender.queuesize config. It is also possible to
     * override global size and configure size for an appender by adding
     * queuesize field to appender definition.
     * <p>
     * When large number of objects are appended to queue with inadequate
     * capacity then application may hang.
     * <p>
     * If size is invalid, then queue is not initialized.
     */
    public void initializeQueue() {
        Validate.validState(configService != null, "configService is null");
        Validate.validState(activityService != null, "activityService is null");

        String queueSize = null;
        try {
            queueSize = configService.getConfig("gotz.appender.queuesize");
        } catch (ConfigNotFoundException e) {
        }
        try {
            queueSize = fieldsHelper.getLastValue("//xf:appender/xf:queueSize",
                    fields);
        } catch (FieldsNotFoundException e) {
        }
        /*
         * default queue size. configService mock returns null so default is set
         * here
         */
        if (StringUtils.isBlank(queueSize)) {
            queueSize = "4096";
        }
        try {
            queue = new ArrayBlockingQueue<>(Integer.parseInt(queueSize));
            LOGGER.debug("created appender [{}] queue size [{}]", name,
                    queueSize);
        } catch (NumberFormatException e) {
            String message = Util.join(
                    "unable to create appender queue [", name, "]");
            LOGGER.error("{}, {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
        }
    }

    /**
     * <p>
     * Get appender queue.
     * @return queue
     */
    public BlockingQueue<Object> getQueue() {
        return queue;
    }

    /**
     * <p>
     * Set appender fields.
     * @param fields
     *            fields
     */
    public void setFields(final Fields fields) {
        Validate.notNull(fields, "fields must not be null");
        this.fields = fields;
    }

    /**
     * <p>
     * Get appender fields.
     * @return fields
     */
    public Fields getFields() {
        return fields;
    }

    /**
     * <p>
     * Set appender name.
     * @param appenderName
     *            name, not null
     */
    public void setName(final String appenderName) {
        Validate.notNull(appenderName, "appenderName must not be null");
        this.name = appenderName;
    }
}
