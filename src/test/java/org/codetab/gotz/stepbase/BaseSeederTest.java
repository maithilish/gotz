package org.codetab.gotz.stepbase;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.gotz.steps.LocatorSeeder;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * BaseSeeder tests.
 * @author Maithilish
 *
 */
public class BaseSeederTest {

    private BaseSeeder baseSeeder;

    @Before
    public void setUp() throws Exception {
        baseSeeder = new LocatorSeeder();
    }

    @Test
    public void testLoad() {
        boolean actual = baseSeeder.load();

        assertThat(actual).isFalse();
    }

    @Test
    public void testStore() {
        boolean actual = baseSeeder.store();

        assertThat(actual).isFalse();
    }

}
