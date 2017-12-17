package org.codetab.gotz.step.load.appender;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.messages.Messages;
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

    @Override
    public void init() {
        setInitialized(true);
        LOGGER.info(Messages.getString("DbAppender.0"), //$NON-NLS-1$
                this.getClass().getSimpleName(), getName());
    }

    /**
     * Creates a file (PrintWriter) from appenders file field. Write the objects
     * taken from blocking queue until object is Marker.EOF.
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

                if (item instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<DataSet> dataSets = (List<DataSet>) item;
                    try {
                        dataSetPersistence.storeDataSet(dataSets);
                    } catch (StepPersistenceException e) {
                        String message =
                                Util.join(Messages.getString("DbAppender.1"), //$NON-NLS-1$
                                        getName(), "]"); //$NON-NLS-1$
                        activityService.addActivity(Type.FAIL, message, e);
                        LOGGER.error("{} {}", message, Util.getMessage(e)); //$NON-NLS-1$
                        LOGGER.debug("{}", e); //$NON-NLS-1$
                        break;
                    }

                } else {
                    String message = Util.join(
                            Messages.getString("DbAppender.5"), //$NON-NLS-1$
                            getName(), Messages.getString("DbAppender.6")); //$NON-NLS-1$
                    activityService.addActivity(Type.FAIL, message);
                    LOGGER.error("{} {}", message); //$NON-NLS-1$
                    break;
                }
            } catch (InterruptedException e) {
                String message = Util.join(Messages.getString("DbAppender.8"), //$NON-NLS-1$
                        getName(), "]"); //$NON-NLS-1$
                activityService.addActivity(Type.FAIL, message, e);
                LOGGER.error("{} {}", message, Util.getMessage(e)); //$NON-NLS-1$
                LOGGER.debug("{}", e); //$NON-NLS-1$
            }
        }
        LOGGER.info(Messages.getString("DbAppender.12"), //$NON-NLS-1$
                getName(), count - 1);
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
        Validate.notNull(object, Messages.getString("DbAppender.13")); //$NON-NLS-1$
        getQueue().put(object);
    }

}
