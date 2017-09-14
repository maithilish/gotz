package org.codetab.gotz.step.convert;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.base.BaseEncoder;

/**
 * <p>
 * Pass-through encoder appends input object directly to appenders without any
 * processing. Useful for integration tests.
 * @author Maithilish
 *
 */
public class PassThroughEncoder extends BaseEncoder {

    /**
     * input object.
     */
    private Object obj;

    /**
     * Returns this step.
     * @return IStep
     */
    @Override
    public IStep instance() {
        return this;
    }

    /**
     * Append input object directory to appenders without any encoding.
     */
    @Override
    public boolean process() {
        Validate.validState(obj != null, "input must not be null");
        doAppend(obj);
        setStepState(StepState.PROCESS);
        return true;
    }

    /**
     * BaseEncoder uses Data as input. But, steps may pass objects other than
     * data like URL string etc., To handle any object, method is overridden to
     * accept Object.
     */
    @Override
    public void setInput(final Object input) {
        Objects.requireNonNull(input, "input must not be null");
        this.obj = input;
    }
}
