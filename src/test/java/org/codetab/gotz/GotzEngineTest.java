package org.codetab.gotz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codetab.gotz.ext.LocatorSeeder;
import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.MonitorService;
import org.codetab.gotz.shared.StepService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GotzEngineTest {

    @Mock
    private MonitorService monitorService;
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

    @InjectMocks
    GotzEngine gotzEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String seederClass = "org.codetab.gotz.ext.LocatorSeeder";

        // given
        when(configService.getConfig("gotz.seederClass")).thenReturn(seederClass);
        when(stepService.getStep(any(String.class))).thenReturn(locatorSeeder);

        // when
        gotzEngine.start();

        // then
        verify(monitorService).start();
        verify(beanService).init();
        verify(dataDefService).init();

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        verify(configService).init(userProvidedFile, defaultsFile);

        verify(stepService).getStep(seederClass);
        verify(locatorSeeder).instance();

        verify(taskPoolService).waitForFinish();
        verify(appenderService).closeAll();
        verify(appenderPoolService).waitForFinish();

        verify(monitorService).end();
    }
}
