package org.codetab.gotz.appender;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.model.Activity.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Append objects to a list.
 * @author Maithilish
 *
 */
public final class ListAppender extends Appender {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ListAppender.class);

    /**
     * list of appended objects.
     */
    private List<Object> list = new ArrayList<>();

    /**
     * <p>
     * Private constructor.
     */
    @Inject
    private ListAppender() {
    }

    /**
     * Takes objects from blocking queue and adds to list until object is
     * Marker.EOF.
     * @throws RuntimeException
     *             if interrupted
     */
    @Override
    public void run() {
        for (;;) {
            Object item = null;
            try {
                item = getQueue().take();
                if (item == Marker.EOF) {
                    break;
                }
                list.add(item);
            } catch (InterruptedException e) {
                String message = "unable to take object from queue";
                LOGGER.debug("{}", e);
                activityService.addActivity(Type.GIVENUP, message, e);
            }
        }
    }

    /**
     * Puts object to blocking queue.
     */
    @Override
    public void append(final Object object) throws InterruptedException {
        Validate.notNull(object, "object must not be null");
        getQueue().put(object);
    }

    /**
     * <p>
     * Get list.
     * @return list of objects
     */
    public List<Object> getList() {
        return list;
    }
}
