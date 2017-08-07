package org.codetab.gotz.stepbase;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Base Filter.
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
     * @see org.codetab.gotz.step.IStep#isConsistent()
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
     * @see org.codetab.gotz.step.IStep#handover()
     */
    @Override
    public boolean handover() {
        Validate.validState(data != null, "data must not be null");
        List<FieldsBase> nextStepFields = createNextStepFields();
        stepService.pushTask(this, data, nextStepFields);
        return true;
    }

    /**
     * <p>
     * Create fields for next step. Uses this step fields for next step also.
     * Throws StepRunException if list is empty.
     * @return list of fields.
     */
    private List<FieldsBase> createNextStepFields() {
        List<FieldsBase> nextStepFields = getFields();
        if (nextStepFields.size() == 0) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
        return nextStepFields;
    }

    /**
     * <p>
     * Sets step input if it is not null and instance of Data.
     *
     * @see org.codetab.gotz.step.IStepO#setInput(java.lang.Object)
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

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean store() {
        return false;
    }

}
