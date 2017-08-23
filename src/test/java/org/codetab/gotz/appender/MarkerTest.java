package org.codetab.gotz.appender;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.gotz.appender.Appender.Marker;
import org.junit.Test;

/**
 * <p>
 * Marker tests.
 * @author Maithilish
 *
 */
public class MarkerTest {

    @Test
    public void test() {

        assertThat(Marker.EOF).isEqualTo(Marker.values()[0]);

        assertThat(Marker.EOF).isEqualTo(Marker.valueOf("EOF"));
    }

}
