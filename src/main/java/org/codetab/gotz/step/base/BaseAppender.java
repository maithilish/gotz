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
        Validate.validState(getFields() != null, "fields must not be null");
        try {
            List<Fields> appenders = fieldsHelper.split(
                    Util.join("/xf:fields/xf:task/xf:steps/xf:step[@name='",
                            getStepType(), "']/xf:appender"),
                    getFields());

            for (Fields fields : appenders) {
                try {
                    String appenderName = fieldsHelper.getLastValue(
                            "/xf:fields/xf:appender/@name", fields);
                    appenderService.createAppender(appenderName, fields);
                    appenderNames.add(appenderName);
                    appenderFieldsMap.put(appenderName, fields);
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | FieldsNotFoundException e) {
                    String message = "unable to append";
                    LOGGER.error("{} {}", message, Util.getMessage(e));
                    LOGGER.debug("{}", e);
                    activityService.addActivity(Type.GIVENUP, message, e);
                }
            }
        } catch (FieldsException e) {
            String message = "unable to find appender fields";
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
            throw new StepRunException(message, e);
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
        Objects.requireNonNull(input, "input must not be null");
        if (input instanceof Data) {
            data = (Data) input;
        } else {
            throw new StepRunException("step input is not Data");
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
     * @throws StepRunException
     *             if append is interrupted
     */
    protected void doAppend(final Object obj) {
        for (String appenderName : appenderNames) {
            Appender appender = appenderService.getAppender(appenderName);
            try {
                appender.append(obj);
            } catch (InterruptedException e) {
                String message = "unable to append";
                LOGGER.error("{} {}", message, Util.getMessage(e));
                LOGGER.debug("{}", e);
                activityService.addActivity(Type.GIVENUP, message, e);
                throw new StepRunException(message, e);
            }
        }
    }

    protected Object encode(final String appenderName,
            final Fields appenderFields) throws Exception {
        Fields encoderFields = getEncoder(appenderFields);
        String className = fieldsHelper
                .getLastValue("/xf:fields/xf:encoder/@class", encoderFields);
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
            encoders = fieldsHelper.split(
                    Util.join("/xf:fields/xf:appender/xf:encoder"),
                    appenderFields);
        } catch (FieldsException e) {
        }
        int size = encoders.size();
        switch (size) {
        case 0:
            throw new FieldsException("no encoder defined");
        case 1:
            return encoders.get(0);
        default:
            throw new FieldsException("more than one encoder defined");
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
