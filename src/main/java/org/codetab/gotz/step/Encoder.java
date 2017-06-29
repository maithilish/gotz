package org.codetab.gotz.step;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.OFieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Encoder extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Encoder.class);

    private Data data;

    private final List<String> appenderNames = new ArrayList<>();

    @Inject
    private AppenderService appenderService;

    @Override
    public boolean initialize() {
        try {
            List<FieldsBase> appenders =
                    FieldsUtil.filterByName(getFields(), "appender");
            for (FieldsBase appender : appenders) {
                List<FieldsBase> fields = OFieldsUtil.asList(appender);
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

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#load()
     */
    @Override
    public boolean load() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#store()
     */
    @Override
    public boolean store() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.StepO#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#setInput(java.lang.Object)
     */
    @Override
    public void setInput(final Object input) {
        if (input instanceof Data) {
            data = (Data) input;
        } else {
            LOGGER.error("input is not instance of Data type. {}",
                    input.getClass().toString());
        }
    }


    protected Data getData() {
        return data;
    }

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
}
