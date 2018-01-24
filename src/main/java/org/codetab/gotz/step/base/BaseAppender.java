package org.codetab.gotz.step.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.step.load.encoder.IEncoder;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Base Encoder. Implementing class encodes to required format (for
 * example, csv).
 * @author Maithilish
 *
 */
public abstract class BaseAppender extends Step {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(BaseAppender.class);

    /**
     * step input - converted Data.
     */
    private Data data;

    /**
     * list of appender names.
     */
    private final List<String> appenderNames = new ArrayList<>();

    /**
     * appenders fields map.
     */
    private final Map<String, Fields> appenderFieldsMap = new HashMap<>();

    /**
     * appender Service.
     */
    @Inject
    private AppenderService appenderService;

    /**
     * Obtains list of appenders defined for the step and for each appender name
     * creates appender and adds its name to appender names list.
     * @throws StepRunException
     *             if no appender field defined for the step
     */
    @Override
    public boolean initialize() {
        Validate.validState(getFields() != null,
                Messages.getString("BaseAppender.0")); //$NON-NLS-1$
        try {
            String xpath =
                    Util.join("/xf:fields/xf:task/xf:steps/xf:step[@name='", //$NON-NLS-1$
                            getStepType(), "']/xf:appender"); //$NON-NLS-1$
            List<Fields> appenders = fieldsHelper.split(xpath, getFields());
            String appenderName = null;
            for (Fields fields : appenders) {
                try {
                    appenderName = ""; // reset prev name //$NON-NLS-1$
                    xpath = "/xf:fields/xf:appender/@name"; //$NON-NLS-1$
                    appenderName = fieldsHelper.getLastValue(xpath, fields);
                    appenderService.createAppender(appenderName, fields);
                    appenderNames.add(appenderName);
                    appenderFieldsMap.put(appenderName, fields);
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | FieldsNotFoundException e) {
                    String label = getLabel();
                    String message =
                            Util.join(Messages.getString("BaseAppender.5"), //$NON-NLS-1$
                                    appenderName, "]"); //$NON-NLS-1$
                    message = getLabeled(message);
                    LOGGER.error("[{}] {} {}", message, e.getMessage()); //$NON-NLS-1$
                    LOGGER.debug("[{}] {}", label, e); //$NON-NLS-1$
                    activityService.addActivity(Type.FAIL, label, message, e);
                }
            }
        } catch (FieldsException e) {
            throw new StepRunException(Messages.getString("BaseAppender.9"), e); //$NON-NLS-1$
        }
        setStepState(StepState.INIT);
        return true;
    }

    /**
     * <p>
     * If input is not null and is instance of Data, sets it as step input.
     */
    @Override
    public void setInput(final Object input) {
        Objects.requireNonNull(input, Messages.getString("BaseAppender.10")); //$NON-NLS-1$
        if (input instanceof Data) {
            data = (Data) input;
        } else {
            String message = Util.join(Messages.getString("BaseAppender.11"), //$NON-NLS-1$
                    input.getClass().getName());
            throw new StepRunException(message);
        }
    }

    /**
     * <p>
     * Return true if step is consistent and data is not null.
     * @return true if data is not null else false
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /**
     * <p>
     * Get data for use by subclasses.
     * @return data
     */
    protected Data getData() {
        return data;
    }

    /**
     * <p>
     * Get encoded data for use by subclasses.
     * @return data
     */
    protected Object getEncodedData() {
        return data;
    }

    /**
     * <p>
     * Appends an object to each of appender defined in appenderNames list.
     *
     * @param obj
     *            the object to append
     * @throws InterruptedException
     * @throws StepRunException
     *             if append is interrupted
     */
    protected void doAppend(final Appender appender, final Object obj)
            throws InterruptedException {
        appender.append(obj);
    }

    protected Appender getAppender(final String appenderName) {
        Appender appender = appenderService.getAppender(appenderName);
        if (appender == null) {
            throw new NullPointerException(Util.join(
                    Messages.getString("BaseAppender.12"), appenderName, "]")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return appender;
        }
    }

    protected Object encode(final String appenderName,
            final Fields appenderFields) throws Exception {
        Fields encoderFields = getEncoder(appenderFields);
        String className = fieldsHelper
                .getLastValue("/xf:fields/xf:encoder/@class", encoderFields); //$NON-NLS-1$
        @SuppressWarnings("rawtypes")
        IEncoder encoderInstance =
                (IEncoder) stepService.createInstance(className);
        encoderInstance.setFields(encoderFields);
        encoderInstance.setLabels(getLabels());
        return encoderInstance.encode(data);
    }

    private Fields getEncoder(final Fields appenderFields)
            throws FieldsException {
        List<Fields> encoders = new ArrayList<>();
        try {
            encoders = fieldsHelper.split("/xf:fields/xf:appender/xf:encoder", //$NON-NLS-1$
                    appenderFields);
        } catch (FieldsException e) {
        }
        int size = encoders.size();
        switch (size) {
        case 0:
            throw new FieldsException(Messages.getString("BaseAppender.16")); //$NON-NLS-1$
        case 1:
            return encoders.get(0);
        default:
            throw new FieldsException(Messages.getString("BaseAppender.17")); //$NON-NLS-1$
        }
    }

    /**
     * Returns map of appender names and its fields.
     * @return map
     */
    protected Map<String, Fields> getAppenderFieldsMap() {
        return appenderFieldsMap;
    }

    /**
     * Do nothing.
     * @return false
     */
    @Override
    public boolean load() {
        return false;
    }

    /**
     * Do nothing.
     * @return false
     */
    @Override
    public boolean store() {
        return false;
    }

    /**
     * Do nothing.
     * @return false
     */
    @Override
    public boolean handover() {
        return false;
    }
}
