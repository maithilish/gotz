package org.codetab.gotz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

public class BaseTest {

    private EnhancedBase base;
    private EnhancedBase base2;

    class EnhancedBase extends Base{
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    @Before
    public void setUp() {
        base = new EnhancedBase();
        base.setId(1L);
        base.setName("tname");

        base2 = new EnhancedBase();
        base2.setId(1L);
        base2.setName("tname");
        base2.dnDetachedState = 21;
        base2.dnFlags = 22;
        base2.dnStateManager = 23;
    }

    @Test
    public void testBase() {
        assertEquals(Long.valueOf(1), base.getId());
        assertEquals("tname", base.getName());
    }

    @Test
    public void testEqualsSymetry() {
        assertTrue(base.equals(base2));
        assertTrue(base2.equals(base));
    }

    @Test
    public void testHashCode() {
        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expected = HashCodeBuilder.reflectionHashCode(base, excludes);

        assertEquals(expected, base.hashCode());
        assertEquals(base.hashCode(),base2.hashCode());
    }

    @Test
    public void testToString() {
        String expected = ToStringBuilder.reflectionToString(base, ToStringStyle.MULTI_LINE_STYLE);
        assertEquals(expected,base.toString());
    }

}
