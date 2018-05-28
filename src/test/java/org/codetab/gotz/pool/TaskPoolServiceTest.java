package org.codetab.gotz.pool;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.gotz.metrics.MetricsHelper;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * for coverage.
 * @author Maithilish
 *
 */
public class TaskPoolServiceTest {

    @Mock
    private ConfigService configService;
    @Mock
    private MetricsHelper metricsHelper;

    @InjectMocks
    private TaskPoolService pools;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubmit() {
        String poolName = "x";
        Runnable task = () -> {
        };

        boolean actual = pools.submit(poolName, task);

        assertThat(actual).isTrue();
    }
}
