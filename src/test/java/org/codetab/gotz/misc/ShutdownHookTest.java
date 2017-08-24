package org.codetab.gotz.misc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.ActivityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * ShutdownHook tests.
 * @author Maithilish
 *
 */
public class ShutdownHookTest {

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ShutdownHook shutdownHook;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() throws InterruptedException {
        shutdownHook.start();
        shutdownHook.join();

        InOrder inOrder = inOrder(activityService);
        inOrder.verify(activityService).logActivities();
        inOrder.verify(activityService).logMemoryUsage();
    }

    @Test
    public void testShutdownHook() {
        assertThat(shutdownHook).isInstanceOf(Thread.class);
    }

    @Test
    public void testShutdownHookSingleton() {
        DInjector dInjector = new DInjector();
        assertThat(dInjector.instance(ShutdownHook.class))
                .isSameAs(dInjector.instance(ShutdownHook.class));
    }

}
