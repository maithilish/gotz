package org.codetab.gotz.step.load.appender;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.persistence.DataSetPersistence;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * File based appender. Writes output to file.
 * @author Maithilish
 *
 */
public final class DbAppender extends Appender {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DbAppender.class);

    /**
     * DataSetPersistence.
     */
    @Inject
    private DataSetPersistence dataSetPersistence;

    /**
     * <p>
     * private constructor.
     */
    @Inject
    private DbAppender() {
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
                    break;
                }

                if (item instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<DataSet> dataSets = (List<DataSet>) item;
                    try {
                        dataSetPersistence.storeDataSet(dataSets);
                    } catch (StepPersistenceException e) {
                        String message = "unable to persist dataset";
                        LOGGER.debug("{}", e);
                        activityService.addActivity(Type.GIVENUP, message, e);
                        break;
                    }

                } else {
                    String message = Util.join(
                            "unable to persist, appended object is not list of DataSet [",
                            item.toString(), "]");
                    activityService.addActivity(Type.GIVENUP, message);
                    break;
                }
            } catch (InterruptedException e) {
                String message = "unable to take object from queue";
                LOGGER.debug("{}", e);
                activityService.addActivity(Type.GIVENUP, message, e);
            }
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
