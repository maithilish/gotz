package org.codetab.gotz.model.iterator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * NullIterator tests.
 * @author Maithilish
 *
 */
public class NullIteratorTest {

    @Test
    public void testHasNext() {
        NullIterator ite = new NullIterator();
        assertThat(ite.hasNext()).isFalse();
    }

    @Test
    public void testNext() {
        NullIterator ite = new NullIterator();
        assertThat(ite.next()).isNull();
    }

}
