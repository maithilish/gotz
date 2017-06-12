package org.codetab.gotz.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.TestTimedOutException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class TaskPoolServiceTest {

    @Mock
    private ConfigService configService;

    @Spy
    Map<String, ExecutorService> executorsMap;

    @Spy
    List<Future<?>> futures;

    @Mock
    ExecutorService executor;

    @InjectMocks
    TaskPoolService taskPool;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubmitResult() {
        String poolName = "x";
        Runnable task = () -> {
        };

        taskPool = createTaskPool(); // without mocks

        boolean actual = taskPool.submit(poolName, task);

        assertThat(actual).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubmitNewPool() {
        String poolName = "x";
        Runnable task = () -> {
        };
        @SuppressWarnings("rawtypes")
        Future future = new CompletableFuture<>();

        given(executorsMap.get(poolName)).willReturn(executor);
        given(executor.submit(task)).willReturn(future);
        given(futures.add(future)).willReturn(true);

        boolean actual = taskPool.submit(poolName, task);
        assertThat(actual).isTrue();

        InOrder inOrder = inOrder(executor, futures);
        inOrder.verify(executor).submit(task);
        inOrder.verify(futures).add(future);
    }

    @Test
    public void testIsDone() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        taskPool = createTaskPool(); // without mocks

        taskPool.submit("x", task1);
        taskPool.submit("x", task2);

        assertThat(taskPool.isDone()).isFalse();

        task1.requestFinish();
        sleep(200);
        assertThat(taskPool.isDone()).isFalse();

        task2.requestFinish();
        sleep(200);
        assertThat(taskPool.isDone()).isTrue();
    }

    @Test
    public void testShutdownAll() {
        List<ExecutorService> executors = new ArrayList<>();

        ExecutorService e1 = Executors.newFixedThreadPool(2);
        ExecutorService e2 = Executors.newFixedThreadPool(2);
        executors.add(e1);
        executors.add(e2);

        given(executorsMap.values()).willReturn(executors);

        assertThat(e1.isShutdown()).isFalse();
        assertThat(e2.isShutdown()).isFalse();

        taskPool.shutdownAll();

        assertThat(e1.isShutdown()).isTrue();
        assertThat(e2.isShutdown()).isTrue();
    }

    @Test(timeout = 1000)
    public void testWaitForFinish() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        taskPool = createTaskPool(); // without mocks

        taskPool.submit("x", task1);
        taskPool.submit("x", task2);

        exceptionRule.expect(TestTimedOutException.class);
        taskPool.waitForFinish();
    }

    @Test(timeout = 5000)
    public void testWaitForFinishTaskFinished() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        taskPool = createTaskPool(); // without mocks

        taskPool.submit("x", task1);
        taskPool.submit("x", task2);

        task1.requestFinish();
        task2.requestFinish();
        sleep(200);

        taskPool.waitForFinish();
        assertThat(taskPool.isDone()).isTrue();
    }

    @Test(timeout = 5000)
    public void testWaitForFinishEndTaskFromOtherThread() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        taskPool = createTaskPool(); // without mocks

        taskPool.submit("x", task1);
        taskPool.submit("x", task2);

        assertThat(taskPool.isDone()).isFalse();

        Runnable task = () -> {
            sleep(1000);
            task1.requestFinish();
            sleep(1000);
            task2.requestFinish();
        };
        new Thread(task).run();

        taskPool.waitForFinish();
        assertThat(taskPool.isDone()).isTrue();
    }

    private void sleep(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    /*
     * TaskPoolService without mocks (futures, executorMap etc) useful for
     * executor related tests
     */
    private TaskPoolService createTaskPool() {
        DInjector di = new DInjector();

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        ConfigService cs = di.instance(ConfigService.class);
        cs.init(userProvidedFile, defaultsFile);

        return di.instance(TaskPoolService.class);
    }

    private class TestTask implements Runnable {

        private boolean finished = false;

        @Override
        public void run() {
            while (!isFinished()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }

        public synchronized void requestFinish() {
            finished = true;
        }

        public synchronized boolean isFinished() {
            return finished;
        }
    }
}