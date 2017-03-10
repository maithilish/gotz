package in.m.picks.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Data;
import in.m.picks.shared.MonitorService;
import in.m.picks.util.Util;

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
            MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
        }
    }

    protected abstract void filter() throws Exception;

    private void initialize() throws FieldNotFoundException {

    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return (super.isConsistent() && data != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#handover()
     */
    @Override
    public void handover() throws Exception {
        pushTask(data, getFields());
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#setInput(java.lang.Object)
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
     * @see in.m.picks.step.IStep#load()
     */
    @Override
    public void load() throws Exception {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see in.m.picks.step.IStep#store()
     */
    @Override
    public void store() throws Exception {
        throw new UnsupportedOperationException();
    }

}
