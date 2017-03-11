package org.codetab.nscoop.step;

import javax.naming.OperationNotSupportedException;

import org.codetab.nscoop.model.Data;
import org.codetab.nscoop.model.Activity.Type;
import org.codetab.nscoop.shared.MonitorService;
import org.codetab.nscoop.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Transformer extends Step {

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
            MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
        }
    }

    protected abstract void transform() throws Exception;

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#load()
     */
    @Override
    public void load() throws Exception {
        throw new OperationNotSupportedException("nothing to load");
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#store()
     */
    @Override
    public void store() throws Exception {
        throw new OperationNotSupportedException("nothing to store");
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.Step#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#setInput(java.lang.Object)
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
