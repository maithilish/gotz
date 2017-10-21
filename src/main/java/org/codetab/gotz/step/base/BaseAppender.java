package org.codetab.gotz.step.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.FieldsUtil;
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
     * step input. It is converted data can be of any type (not gotz.model.Data
     * type)
     */
    private Object data;

    /**
     * list of appender names.
     */
    private final List<String> appenderNames = new ArrayList<>();

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
            List<FieldsBase> appenders =
                    FieldsUtil.filterByName(getFields(), "appender");
            for (FieldsBase appender : appenders) {
                List<FieldsBase> fields = FieldsUtil.asList(appender);
                try {
                    String appenderName = appender.getValue();
                    appenderService.createAppender(appenderName, fields);
                    appenderNames.add(appenderName);
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | FieldNotFoundException e) {
                    String message = "unable to append";
                    LOGGER.error("{} {}", message, Util.getMessage(e));
                    LOGGER.debug("{}", e);
                    activityService.addActivity(Type.GIVENUP, message, e);
                }
            }
        } catch (FieldNotFoundException e) {
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
        data = input;

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
     * Get dataSets for use by subclasses.
     * @return data
     */
    protected Object getData() {
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
