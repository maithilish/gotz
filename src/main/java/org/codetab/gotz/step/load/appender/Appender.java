package org.codetab.gotz.step.load.appender;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.messages.Messages;
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

    private boolean initialized = false;

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

    public abstract void init();

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
        Validate.validState(configService != null,
                Messages.getString("Appender.0")); //$NON-NLS-1$
        Validate.validState(activityService != null,
                Messages.getString("Appender.1")); //$NON-NLS-1$

        String queueSize = null;
        try {
            queueSize = configService.getConfig("gotz.appender.queuesize"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
        }
        try {
            queueSize = fieldsHelper.getLastValue(
                    "/xf:fields/xf:appender/xf:queueSize", fields); //$NON-NLS-1$
        } catch (FieldsNotFoundException e) {
        }
        /*
         * default queue size. configService mock returns null so default is set
         * here
         */
        if (StringUtils.isBlank(queueSize)) {
            queueSize = "4096"; //$NON-NLS-1$
        }
        try {
            queue = new ArrayBlockingQueue<>(Integer.parseInt(queueSize));
            LOGGER.info(Messages.getString("Appender.5"), name, //$NON-NLS-1$
                    queueSize);
        } catch (NumberFormatException e) {
            String message =
                    Util.join(Messages.getString("Appender.6"), name, "]"); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("{}, {}", message, Util.getMessage(e)); //$NON-NLS-1$
            LOGGER.debug("{}", e); //$NON-NLS-1$
            activityService.addActivity(Type.FAIL, message, e);
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
        Validate.notNull(fields, Messages.getString("Appender.10")); //$NON-NLS-1$
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
     * Get appender name.
     * @return name of appender
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Set appender name.
     * @param appenderName
     *            name, not null
     */
    public void setName(final String appenderName) {
        Validate.notNull(appenderName, Messages.getString("Appender.11")); //$NON-NLS-1$
        this.name = appenderName;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(final boolean initialized) {
        this.initialized = initialized;
    }

}
