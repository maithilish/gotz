package org.codetab.gotz.step.load.appender;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Activity.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Takes objects from queue and append them to a list, useful for integration
 * tests.
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

    @Override
    public void init() {
        setInitialized(true);
        LOGGER.info(Messages.getString("ListAppender.0"), //$NON-NLS-1$
                this.getClass().getSimpleName(), getName());
    }

    /**
     * Takes objects from blocking queue and adds to list until object is
     * Marker.EOF.
     * @throws RuntimeException
     *             if interrupted
     */
    @Override
    public void run() {
        int count = 0;
        for (;;) {
            Object item = null;
            try {
                item = getQueue().take();
                count++;
                if (item == Marker.EOF) {
                    break;
                }
                list.add(item);
            } catch (InterruptedException e) {
                String message = Messages.getString("ListAppender.1"); //$NON-NLS-1$
                LOGGER.debug("{}", e); //$NON-NLS-1$
                activityService.addActivity(Type.FAIL, message, e);
            }
        }
        LOGGER.info(Messages.getString("ListAppender.3"), //$NON-NLS-1$
                getName(), count - 1);
    }

    /**
     * Puts object to blocking queue.
     */
    @Override
    public void append(final Object object) throws InterruptedException {
        Validate.notNull(object, Messages.getString("ListAppender.4")); //$NON-NLS-1$
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
