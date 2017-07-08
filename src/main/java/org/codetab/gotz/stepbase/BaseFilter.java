package org.codetab.gotz.stepbase;

import java.util.List;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseFilter extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseFilter.class);

    private Data data;

    @Override
    public boolean initialize() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#handover()
     */
    @Override
    public boolean handover() {
        List<FieldsBase> nextStepFields = createNextStepFields();
        stepService.pushTask(this, data, nextStepFields);
        return true;
    }

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

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#setInput(java.lang.Object)
     */
    @Override
    public void setInput(final Object input) {
        if (input instanceof Data) {
            this.data = (Data) input;
        } else {
            LOGGER.error("input is not instance of Data {}",
                    input.getClass().toString());
        }
    }

    /*
     *
     */
    public Data getData() {
        return data;
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

}
