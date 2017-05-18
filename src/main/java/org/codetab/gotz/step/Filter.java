package org.codetab.gotz.step;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Filter extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Filter.class);

    private Data data;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            initialize();
            filter();
            setConsistent(true);
            handover();
        } catch (Exception e) {
            String message = "parse data " + Util.getLocatorLabel(getFields());
            LOGGER.error("{} {}", message, Util.getMessage(e));
            LOGGER.debug("{}", e);
            activityService.addActivity(Type.GIVENUP, message, e);
        }
    }

    protected abstract void filter() throws Exception;

    private void initialize() throws FieldNotFoundException {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#handover()
     */
    @Override
    public void handover(){
        pushTask(data, getFields());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#setInput(java.lang.Object)
     */
    @Override
    public void setInput(final Object input) {
        if (input instanceof Data) {
            this.data = (Data) input;
        } else {
            LOGGER.error("input is not instance of Data {}", input.getClass().toString());
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
     * @see org.codetab.gotz.step.IStep#load()
     */
    @Override
    public void load(){
        throw new RuntimeException("not supported operation");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#store()
     */
    @Override
    public void store(){
        throw new RuntimeException("not supported operation");
    }

}
