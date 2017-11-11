package org.codetab.gotz.appender;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.XFieldException;
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

    /**
     * IOHelper.
     */
    @Inject
    private IOHelper ioHelper;

    /**
     * <p>
     * private constructor.
     */
    @Inject
    private FileAppender() {
    }

    /**
     * Creates a file (PrintWriter) from appenders file field. Write the objects
     * taken from blocking queue until object is Marker.EOF.
     */
    @Override
    public void run() {
        Validate.validState(ioHelper != null, "ioHelper is null");

        String fileName = null;
        try {
            fileName =
                    xFieldHelper.getLastValue("//:appender/:file", getXField());
        } catch (XFieldException e) {
            String message = "file appender ";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            return;
        }

        try (PrintWriter writer = ioHelper.getPrintWriter(fileName);) {
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
                    String message = "unable to take object from queue";
                    LOGGER.debug("{}", e);
                    activityService.addActivity(Type.GIVENUP, message, e);
                }
            }
        } catch (IOException e) {
            String message = "file appender ";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
        }
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
        getQueue().put(object);
    }

}
