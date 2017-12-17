package org.codetab.gotz;

import java.io.Console;
import java.util.Date;

import javax.inject.Inject;

import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.messages.Messages;
import org.codetab.gotz.misc.ShutdownHook;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Task;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(GSystem.class);

    @Inject
    private ConfigService configService;
    @Inject
    private BeanService beanService;
    @Inject
    private DataDefService dataDefService;
    @Inject
    private StepService stepService;
    @Inject
    private LocatorFieldsHelper locatorFieldsHelper;

    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    public GSystem() {
    }

    /*
     *
     */
    public boolean initSystem() {
        LOGGER.info(Messages.getString("GSystem.0")); //$NON-NLS-1$
        runTime.addShutdownHook(shutdownHook);

        String userProvidedFile = "gotz.properties"; //$NON-NLS-1$
        String defaultsFile = "gotz-default.xml"; //$NON-NLS-1$
        configService.init(userProvidedFile, defaultsFile);

        String mode = getMode();
        LOGGER.info(mode);
        Date runDate = configService.getRunDate();
        LOGGER.info(Messages.getString("GSystem.3"), runDate); //$NON-NLS-1$

        try {
            String beanFile = configService.getConfig("gotz.beanFile"); //$NON-NLS-1$
            String schemaFile = configService.getConfig("gotz.schemaFile"); //$NON-NLS-1$
            beanService.init(beanFile, schemaFile);
        } catch (ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("GSystem.1"), e); //$NON-NLS-1$
        }

        dataDefService.init();
        int dataDefsCount = dataDefService.getCount();
        LOGGER.info(Messages.getString("GSystem.7"), dataDefsCount); //$NON-NLS-1$

        locatorFieldsHelper.init();

        return true;
    }

    /*
     *
     */
    public Task createInitialTask() {
        LOGGER.info(Messages.getString("GSystem.8")); //$NON-NLS-1$
        try {
            String seederClassName =
                    configService.getConfig("gotz.seederClass"); //$NON-NLS-1$
            IStep step = stepService.getStep(seederClassName).instance();
            Task task = stepService.createTask(step);
            return task;
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("GSystem.10"), e); //$NON-NLS-1$
        }
    }

    private String getMode() {
        String modeInfo = Messages.getString("GSystem.11"); //$NON-NLS-1$
        if (configService.isTestMode()) {
            modeInfo = Messages.getString("GSystem.12"); //$NON-NLS-1$
        }
        if (configService.isDevMode()) {
            modeInfo = Messages.getString("GSystem.13"); //$NON-NLS-1$
        }
        return modeInfo;
    }

    public void waitForHeapDump() {
        String wait = "false"; //$NON-NLS-1$
        try {
            wait = configService.getConfig("gotz.waitForHeapDump"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
        }
        if (wait.equalsIgnoreCase("true")) { //$NON-NLS-1$
            System.gc();
            Console console = System.console();
            console.printf("%s%s", Messages.getString("GSystem.18"), Util.LINE); //$NON-NLS-1$ //$NON-NLS-2$
            console.printf("%s", Messages.getString("GSystem.20")); //$NON-NLS-1$ //$NON-NLS-2$
            console.readLine();
        }
    }
}
