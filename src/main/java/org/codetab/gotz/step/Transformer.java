package org.codetab.gotz.step;

import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Transformer extends StepO {

    static final Logger LOGGER = LoggerFactory.getLogger(Transformer.class);

    private Data data;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        processStep();
    }

    // template method
    private void processStep() {
        try {
            transform();
            handover();
        } catch (Exception e) {
            String message = "transform data " + Util.getLocatorLabel(getFields());
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
        }
    }

    protected abstract void transform() throws Exception;

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#load()
     */
    @Override
    public void load() {
        throw new RuntimeException("not supported operation");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#store()
     */
    @Override
    public void store() {
        throw new RuntimeException("not supported operation");
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

    /*
     *
     */
    protected Data getData() {
        return data;
    }

}
