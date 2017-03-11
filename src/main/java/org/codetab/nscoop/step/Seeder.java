package org.codetab.nscoop.step;

import org.codetab.nscoop.model.Activity.Type;
import org.codetab.nscoop.shared.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Seeder extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(Seeder.class);

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
            load();
            setConsistent(true);
            handover();
        } catch (Exception e) {
            LOGGER.warn("{}", e.getLocalizedMessage());
            LOGGER.debug("{}", e);
            MonitorService.INSTANCE.addActivity(Type.GIVENUP, "unable to seed", e);

        }
    }

}
