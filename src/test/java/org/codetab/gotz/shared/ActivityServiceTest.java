package org.codetab.gotz.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Timer;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.misc.MemoryTask;
import org.codetab.gotz.model.Activity;
import org.codetab.gotz.model.Activity.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ActivityServiceTest {

    @Mock
    Timer timer;
    @Mock
    StopWatch stopWatch;
    @Mock
    MemoryTask memoryTask;

    @Mock
    List<Activity> activities;
    @Mock
    LongSummaryStatistics totalMemory;
    @Mock
    LongSummaryStatistics freeMemory;
    @Mock
    Runtime runtime;

    @InjectMocks
    private ActivityService activityService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        ActivityService instanceA = dInjector.instance(ActivityService.class);
        ActivityService instanceB = dInjector.instance(ActivityService.class);

        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }


    @Test
    public void testStart() {
        // when
        activityService.start();

        // then
        verify(stopWatch).start();
        verify(timer).schedule(memoryTask, 0L, 5000L);
    }

    @Test
    public void testEnd() {
        // when
        activityService.end();

        // then
        verify(stopWatch).stop();
        verify(timer).cancel();
    }

    @Test
    public void testAddActivity() throws IllegalAccessException {
        // when
        activityService.addActivity(Type.GIVENUP, "tmessage");

        // then
        ArgumentCaptor<Activity> argument = ArgumentCaptor.forClass(Activity.class);
        verify(activities).add(argument.capture());
        assertThat(argument.getValue().getType()).isEqualTo(Type.GIVENUP);
        assertThat(argument.getValue().getMessage()).isEqualTo("tmessage");
        assertThat(argument.getValue().getThrowable()).isNull();
    }

    @Test
    public void testAddActivityWithThrowable() throws IllegalAccessException {
        // given
        Throwable throwable = new Throwable("foo");

        // when
        activityService.addActivity(Type.GIVENUP, "tmessage", throwable); // when

        // then
        ArgumentCaptor<Activity> argument = ArgumentCaptor.forClass(Activity.class);
        verify(activities).add(argument.capture());
        assertThat(argument.getValue().getType()).isEqualTo(Type.GIVENUP);
        assertThat(argument.getValue().getMessage()).isEqualTo("tmessage");
        assertThat(argument.getValue().getThrowable()).isSameAs(throwable);
    }

    @Test
    public void testCollectMemoryStat() {
        // given
        given(runtime.totalMemory()).willReturn(1 * 1048576L);
        given(runtime.freeMemory()).willReturn(2 * 1048576L);

        // when
        activityService.collectMemoryStat();

        // then
        verify(totalMemory).accept(1L);
        verify(freeMemory).accept(2L);
    }

}
