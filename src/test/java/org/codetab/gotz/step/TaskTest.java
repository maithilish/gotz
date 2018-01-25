package org.codetab.gotz.step;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.exception.StepPersistenceException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.shared.ActivityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TaskTest {

    @Mock
    private Step step;
    @Mock
    private ActivityService activityService;
    @InjectMocks
    private Task task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun() {
        task.run();

        InOrder inOrder = inOrder(step);
        inOrder.verify(step).initialize();
        inOrder.verify(step).load();
        inOrder.verify(step).process();
        inOrder.verify(step).store();
        inOrder.verify(step).handover();
    }

    @Test
    public void testRunThrowsException() {

        given(step.getLabels()).willReturn(new Labels("n", "g"));
        given(step.initialize()).willThrow(StepRunException.class)
                .willThrow(StepPersistenceException.class)
                .willThrow(IllegalStateException.class);

        task.run();
        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(StepRunException.class));

        task.run();
        verify(activityService).addActivity(eq(Type.FAIL), any(String.class),
                any(String.class), any(StepPersistenceException.class));

        task.run();
        verify(activityService).addActivity(eq(Type.INTERNAL),
                any(String.class), any(String.class),
                any(IllegalStateException.class));

    }

}
