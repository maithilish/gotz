package org.codetab.gotz.pool;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * for coverage.
 * @author Maithilish
 *
 */
public class TaskPoolServiceTest {

    private TaskPoolService pools;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        DInjector di = new DInjector();

        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        ConfigService cs = di.instance(ConfigService.class);
        cs.init(userProvidedFile, defaultsFile);

        pools = di.instance(TaskPoolService.class);
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
