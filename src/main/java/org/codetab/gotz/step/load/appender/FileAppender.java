package org.codetab.gotz.step.load.appender;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.helper.IOHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * File based appender. Writes output to file.
 * @author Maithilish
 *
 */
public final class FileAppender extends Appender {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FileAppender.class);

    @Inject
    private IOHelper ioHelper;

    private PrintWriter writer;

    /**
     * <p>
     * private constructor.
     */
    @Inject
    private FileAppender() {
    }

    @Override
    public void init() {

        Validate.validState(ioHelper != null, "ioHelper is null");

        try {
            String fileName = fieldsHelper.getLastValue(
                    "/xf:fields/xf:appender/xf:file", getFields());
            writer = ioHelper.getPrintWriter(fileName);
            setInitialized(true);
            LOGGER.info("initialized {} [{}]", this.getClass().getSimpleName(),
                    getName());
        } catch (IOException | FieldsNotFoundException e) {
            String message =
                    Util.join("unable to init file appender [", getName(), "]");
            activityService.addActivity(Type.FAIL, message, e);
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
        }
    }

    /**
     * Creates a file (PrintWriter) from appenders file field. Write the objects
     * taken from blocking queue until object is Marker.EOF.
     */
    @Override
    public void run() {
        for (;;) {
            Object item = null;
            try {
                item = getQueue().take();
                if (item == Marker.EOF) {
                    writer.flush();
                    break;
                }
                String data = item.toString();
                writer.println(data);
            } catch (InterruptedException e) {
                String message = Util.join("unable to take object from queue [",
                        getName(), "]");
                activityService.addActivity(Type.FAIL, message, e);
                LOGGER.error("{} {}", message, Util.getMessage(e));
                LOGGER.debug("{}", e);
            }
        }
        writer.close();
    }

    /**
     * Append object to appender queue.
     * @param object
     *            object to append, not null
     * @throws InterruptedException
     *             if interrupted while queue put operation
     */
    @Override
    public void append(final Object object) throws InterruptedException {
        Validate.notNull(object, "object must not be null");
        if (isInitialized()) {
            getQueue().put(object);
        }
    }
}
