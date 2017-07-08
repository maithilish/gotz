package org.codetab.gotz.steps;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassThroughEncoder extends Step {

    static final Logger LOGGER =
            LoggerFactory.getLogger(PassThroughEncoder.class);

    private Object obj;

    private final List<String> appenderNames = new ArrayList<>();

    @Inject
    private AppenderService appenderService;

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean initialize() {
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

    @Override
    public boolean process() {
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
        setStepState(StepState.PROCESS);
        return true;
    }

    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && obj != null);
    }

    @Override
    public boolean load() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean store() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handover() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setInput(final Object input) {
        this.obj = input;
    }
}
