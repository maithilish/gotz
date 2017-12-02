package org.codetab.gotz.step.base;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Base Data Converter. Implementing class converts the Data to other
 * formats.
 * @author Maithilish
 *
 */
public abstract class BaseConverter extends Step {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(BaseConverter.class);

    /**
     * step input.
     */
    private Data data;

    /**
     * step output.
     */
    private Object convertedData;

    /**
     * <p>
     * Push task with data as input. This step fields are used for next step
     * also.
     * @return true
     */
    @Override
    public boolean handover() {
        Validate.validState(convertedData != null,
                "convertedData must not be null");
        Fields nextStepFields = createNextStepFields();
        stepService.pushTask(this, convertedData, getLabels(), nextStepFields);
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
    private Fields createNextStepFields() {
        Fields nextStepFields = getFields();
        if (nextStepFields.getNodes().size() == 0) {
            String message = "unable to get next step fields";
            LOGGER.error("{} {}", message, getLabel());
            activityService.addActivity(Type.GIVENUP, message);
            throw new StepRunException(message);
        }
        return nextStepFields;
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
            LOGGER.error("input is not instance of Data type. {}",
                    input.getClass().toString());
        }
    }

    /**
     * <p>
     * Set converted data by subclasses.
     * @param convertedData
     *            converted data
     */
    protected void setConvertedData(final Object convertedData) {
        this.convertedData = convertedData;
    }

    /**
     * <p>
     * Return true if step is consistent and data is not null.
     * @return true if data is not null else false
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && convertedData != null);
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
     * Do nothing.
     * @return false
     */
    @Override
    public boolean initialize() {
        return false;
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
