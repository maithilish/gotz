package org.codetab.gotz.misc;

import java.util.TimerTask;

import javax.inject.Inject;

import org.codetab.gotz.shared.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryTask extends TimerTask {

    static final Logger LOGGER = LoggerFactory.getLogger(MemoryTask.class);

    private ActivityService activityService;

    @Override
    public void run() {
        activityService.collectMemoryStat();
    }

    @Inject
    public void setActivityService(final ActivityService activityService) {
        this.activityService = activityService;
    }

}
