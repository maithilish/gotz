package org.codetab.gotz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codetab.gotz.exception.FatalException;
import org.codetab.gotz.ext.LocatorSeeder;
import org.codetab.gotz.misc.ShutdownHook;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GotzEngineTest {

    @Mock
    private ActivityService activityService;
    @Mock
    private ConfigService configService;
    @Mock
    private BeanService beanService;
    @Mock
    private DataDefService dataDefService;
    @Mock
    private StepService stepService;
    @Mock
    private TaskPoolService taskPoolService;
    @Mock
    private AppenderPoolService appenderPoolService;
    @Mock
    private AppenderService appenderService;
    @Mock
    private LocatorSeeder locatorSeeder;
    @Mock
    private ShutdownHook shutdownHook;
    @Mock
    private Runtime runTime;

    @InjectMocks
    GotzEngine gotzEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, FatalException {
        String seederClass = "org.codetab.gotz.ext.LocatorSeeder";

        // given
        when(configService.getConfig("gotz.seederClass")).thenReturn(seederClass);
        when(stepService.getStep(any(String.class))).thenReturn(locatorSeeder);

        // when
        gotzEngine.start();

        // then
        verify(activityService).start();

        verify(runTime).addShutdownHook(any(ShutdownHook.class));

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        verify(configService).init(userProvidedFile, defaultsFile);

        verify(beanService).init();
        verify(dataDefService).init();

        verify(configService).getConfig("gotz.seederClass");
        verify(stepService).getStep(seederClass);
        verify(locatorSeeder).instance();

        verify(taskPoolService).waitForFinish();
        verify(appenderService).closeAll();
        verify(appenderPoolService).waitForFinish();

        verify(activityService).end();
    }

    @Test
    public void testStartShouldCatchFatal() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, FatalException {
        String seederClass = "org.codetab.gotz.ext.XYZ";

        // given
        given(configService.getConfig("gotz.seederClass")).willReturn(seederClass);
        given(stepService.getStep(any(String.class))).willThrow(ClassNotFoundException.class);

        // when
        gotzEngine.start();
    }

}
