package org.codetab.gotz.shared;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.codetab.gotz.model.Activity;
import org.codetab.gotz.model.Activity.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MonitorServiceTest {

    @Spy
    private MonitorService monitorServiceMock;

    @InjectMocks
    private MonitorService sut;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        FieldUtils.writeDeclaredField(sut, "INSTANCE", monitorServiceMock, true);
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
    public void testStartStopWatch() throws IllegalAccessException, NoSuchMethodException,
    InvocationTargetException, InstantiationException {
        StopWatch stopWatch = (StopWatch) FieldUtils.readDeclaredField(sut, "stopWatch",
                true);
        then(stopWatch).isNull();

        sut.start(); // when

        stopWatch = (StopWatch) FieldUtils.readDeclaredField(sut, "stopWatch", true);
        then(stopWatch).isNotNull();
        then(stopWatch.isStarted()).isTrue();
    }

    @Test
    public void testStartTimer() throws IllegalAccessException {
        // can't use doReturn on sut so use spy (monitorServiceMock)
        Timer timer = mock(Timer.class);
        doReturn(timer).when(monitorServiceMock).makeTimer(any(String.class));

        monitorServiceMock.start(); // when

        verify(timer).schedule(any(TimerTask.class), eq(0L), eq(5000L));
    }

    @Test
    public void testEndStopWatch() throws IllegalAccessException {
        sut.start();
        StopWatch stopWatch = (StopWatch) FieldUtils.readDeclaredField(sut, "stopWatch",
                true);
        then(stopWatch.isStarted()).isTrue();

        sut.end(); // when

        then(stopWatch.isStopped()).isTrue();
    }

    @Test
    public void testStopTimer() throws IllegalAccessException {
        // can't use doReturn on sut so use spy (monitorServiceMock)
        Timer timer = mock(Timer.class);
        doReturn(timer).when(monitorServiceMock).makeTimer(any(String.class));

        monitorServiceMock.start();
        monitorServiceMock.end(); // when

        verify(timer).cancel();
    }


    @Test
    public void testTriggerFatal() throws IllegalAccessException {
        sut.start();

        StopWatch stopWatch = (StopWatch) FieldUtils.readDeclaredField(sut, "stopWatch",
                true);

        then(stopWatch.isStarted()).isTrue();

        try {
            sut.triggerFatal("tmessage"); // when
            fail("IllegalStateException should be thrown");
        } catch (IllegalStateException e) {
            then(stopWatch.isStopped()).isTrue();
        }
    }

    @Test
    public void testAddActivity() throws IllegalAccessException {

        sut.addActivity(Type.GIVENUP, "tmessage"); // when

        @SuppressWarnings("unchecked")
        List<Activity> list = (List<Activity>) FieldUtils.readDeclaredField(sut,
                "activitesList", true);

        then(list.size()).isEqualTo(1);

        Activity actual = list.get(0);

        then(actual.getType()).isEqualTo(Type.GIVENUP);
        then(actual.getMessage()).isEqualTo("tmessage");
        then(actual.getThrowable()).isNull();
    }

    @Test
    public void testAddActivityWithThrowable() throws IllegalAccessException {

        Throwable throwable = new Throwable("foo");
        sut.addActivity(Type.GIVENUP, "tmessage", throwable); // when

        @SuppressWarnings("unchecked")
        List<Activity> list = (List<Activity>) FieldUtils.readDeclaredField(sut,
                "activitesList", true);

        then(list.size()).isEqualTo(1);

        Activity actual = list.get(0);

        then(actual.getType()).isEqualTo(Type.GIVENUP);
        then(actual.getMessage()).isEqualTo("tmessage");
        then(actual.getThrowable()).isSameAs(throwable);
    }

}
