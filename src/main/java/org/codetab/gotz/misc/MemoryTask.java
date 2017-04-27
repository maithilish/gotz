package org.codetab.gotz.misc;

import java.util.Date;
import java.util.TimerTask;

import javax.inject.Inject;

import org.codetab.gotz.shared.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MemoryTask extends TimerTask {

    static final Logger LOGGER = LoggerFactory.getLogger(MemoryTask.class);

    private MonitorService monitorService;

    @Inject
    void setMonitorService(MonitorService monitorService){
        this.monitorService = monitorService;
    }

    @Override
    public void run() {
        Runtime runtime = Runtime.getRuntime();
        long mm = runtime.maxMemory();
        long fm = runtime.freeMemory();
        long tm = runtime.totalMemory();
        monitorService.pollMemory(mm, tm, fm);
        LOGGER.debug("{} {} {} {}", new Date(), mm, tm, fm);
    }

}
