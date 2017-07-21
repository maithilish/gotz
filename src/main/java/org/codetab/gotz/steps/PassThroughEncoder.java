package org.codetab.gotz.steps;

import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.stepbase.BaseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassThroughEncoder extends BaseEncoder {

    static final Logger LOGGER =
            LoggerFactory.getLogger(PassThroughEncoder.class);

    private Object obj;

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean process() {
        doAppend(obj);
        setStepState(StepState.PROCESS);
        return true;
    }

    @Override
    public void setInput(final Object input) {
        // baseEncoder uses Data as input, but we need object in this class
        this.obj = input;
    }
}
