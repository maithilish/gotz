package org.codetab.gotz;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.step.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GotzEngineTest {

    @Mock
    private Task task;
    @Mock
    private ActivityService activityService;
    @Mock
    private GSystem gSystem;
    @Mock
    private GTaskRunner gTaskRunner;

    @InjectMocks
    private GotzEngine gotzEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {
        // given
        given(gSystem.createInitialTask()).willReturn(task);

        // when
        gotzEngine.start();

        // then
        InOrder inOrder = inOrder(gSystem, gTaskRunner, activityService);
        inOrder.verify(activityService).start();
        inOrder.verify(gSystem).initSystem();
        inOrder.verify(gSystem).createInitialTask();
        inOrder.verify(gSystem).waitForHeapDump();
        inOrder.verify(gTaskRunner).executeInitalTask(task);
        inOrder.verify(gTaskRunner).waitForFinish();
        inOrder.verify(gSystem).waitForHeapDump();
        inOrder.verify(activityService).end();
        verifyNoMoreInteractions(gSystem, gTaskRunner, activityService);
    }

    @Test
    public void testStartShouldCatchException() {
        // given
        given(gSystem.initSystem()).willThrow(CriticalException.class);

        gotzEngine.start();

        // then
        InOrder inOrder = inOrder(gSystem, activityService);
        inOrder.verify(activityService).start();
        inOrder.verify(gSystem).initSystem();
        inOrder.verify(activityService).end();
        verifyNoMoreInteractions(gSystem, gTaskRunner, activityService);
    }

}
