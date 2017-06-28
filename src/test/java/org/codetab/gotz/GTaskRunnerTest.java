package org.codetab.gotz;

import static org.mockito.Mockito.verify;

import org.codetab.gotz.pool.AppenderPoolService;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GTaskRunnerTest {

    @Mock
    Task task;
    @Mock
    private TaskPoolService taskPoolService;
    @Mock
    private AppenderPoolService appenderPoolService;
    @Mock
    private AppenderService appenderService;

    @InjectMocks
    GTaskRunner gTaskRunner;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteInitalTask() {
        // given

        // when
        gTaskRunner.executeInitalTask(task);

        // then
        verify(taskPoolService).submit("seeder", task);
    }

    @Test
    public void testWaitForFinish() {
        // given

        // when
        gTaskRunner.waitForFinish();

        // then
        verify(taskPoolService).waitForFinish();
        verify(appenderService).closeAll();
        verify(appenderPoolService).waitForFinish();
    }

}
