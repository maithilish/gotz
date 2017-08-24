package org.codetab.gotz.misc;

import java.util.TimerTask;

import javax.inject.Inject;

import org.codetab.gotz.shared.ActivityService;

/**
 * <p>
 * Task to collect memory stats.
 * @author Maithilish
 *
 */
public class MemoryTask extends TimerTask {

    /**
     * activity service.
     */
    @Inject
    private ActivityService activityService;

    @Override
    public void run() {
        activityService.collectMemoryStat();
    }
}
