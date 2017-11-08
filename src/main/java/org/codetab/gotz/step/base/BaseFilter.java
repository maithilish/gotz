package org.codetab.gotz.step.base;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Base Filter. Implementing class filters the data.
 * @author Maithilish
 *
 */
public abstract class BaseFilter extends Step {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(BaseFilter.class);

    /**
     * step input and output.
     */
    private Data data;

    @Override
    public boolean initialize() {
        return false;
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
     * Push task with data as input. This step fields are used for next step
     * also.
     * @return true
     */
    @Override
    public boolean handover() {
        Validate.validState(data != null, "data must not be null");
        XField nextStepXField = createNextStepXField();
        stepService.pushTask(this, data, nextStepXField);
        return true;
    }

    /**
     * <p>
     * Create fields for next step. Uses this step fields for next step also.
     * Throws StepRunException if list is empty.
     * @return list of fields
     * @throws StepRunException
     *             if nextStepFields list is empty
     */
    private XField createNextStepXField() {
        XField nextStepXField = getXField();
        if (nextStepXField.getNodes().size() == 0) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
        return nextStepXField;
    }

    /**
     * <p>
     * If input is not null and is instance of Data, sets it as step input.
     *
     */
    @Override
    public void setInput(final Object input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input instanceof Data) {
            this.data = (Data) input;
        } else {
            LOGGER.error("input is not instance of Data {}",
                    input.getClass().toString());
        }
    }

    /**
     * <p>
     * Get data.
     * @return data
     */
    public Data getData() {
        return data;
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
}
