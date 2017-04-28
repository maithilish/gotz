package org.codetab.gotz.shared;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.codetab.gotz.misc.MemoryTask;
import org.codetab.gotz.model.Activity;
import org.codetab.gotz.model.Activity.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MonitorServiceTest {

    @Mock
    Timer timer;

    @Mock
    StopWatch stopWatch;

    @Spy
    List<Activity> activitesList = new ArrayList<>();

    @Spy
    ConfigService configService;

    @InjectMocks
    private MonitorService sut;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMonitorService() throws IllegalAccessException {
        @SuppressWarnings("unchecked")
        List<Activity> list = (List<Activity>) FieldUtils.readDeclaredField(sut,
                "activitesList", true);

        then(list).isNotNull();
        then(list.size()).isEqualTo(0);
    }

    @Test
    public void testStart() {
        // MemoryTask is final so can't mock
        sut.setMemoryTask(new MemoryTask());

        sut.start();

        verify(stopWatch).start();
        verify(timer).schedule(any(MemoryTask.class), eq(0L), eq(5000L));
    }

    @Test
    public void testEnd() {

        sut.end();

        verify(stopWatch).stop();
        verify(timer).cancel();
    }

    @Test
    public void testTriggerFatal() throws IllegalAccessException {
        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        configService.init(userProvidedFile, defaultsFile);

        try {
            sut.triggerFatal("tmessage"); // when
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            verify(stopWatch).stop();
            verify(timer).cancel();
            verify(activitesList).add(any(Activity.class));
        }
    }

    @Test
    public void testAddActivity() throws IllegalAccessException {

        then(activitesList.size()).isEqualTo(0);

        sut.addActivity(Type.GIVENUP, "tmessage"); // when

        then(activitesList.size()).isEqualTo(1);

        Activity actual = activitesList.get(0);

        then(actual.getType()).isEqualTo(Type.GIVENUP);
        then(actual.getMessage()).isEqualTo("tmessage");
        then(actual.getThrowable()).isNull();
    }

    @Test
    public void testAddActivityWithThrowable() throws IllegalAccessException {

        then(activitesList.size()).isEqualTo(0);

        Throwable throwable = new Throwable("foo");
        sut.addActivity(Type.GIVENUP, "tmessage", throwable); // when

        then(activitesList.size()).isEqualTo(1);

        Activity actual = activitesList.get(0);

        then(actual.getType()).isEqualTo(Type.GIVENUP);
        then(actual.getMessage()).isEqualTo("tmessage");
        then(actual.getThrowable()).isSameAs(throwable);
    }

}
