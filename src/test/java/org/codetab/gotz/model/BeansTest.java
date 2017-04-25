package org.codetab.gotz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

public class BeansTest {

    private Beans beans;
    private Beans beans2;

    @Before
    public void setUp() throws Exception {
        Bean bean = new Bean();
        bean.setName("tname");
        beans = new Beans();
        beans.getBean().add(bean);

        Bean bean2 = new Bean();
        bean2.setName("tname");
        beans2 = new Beans();
        beans2.getBean().add(bean);
    }

    @Test
    public void testHashCode() {
        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expected =  HashCodeBuilder.reflectionHashCode(beans, excludes);

        assertEquals(expected,beans.hashCode());
    }

    @Test
    public void testGetBean() {
        beans = new Beans();
        assertEquals(0,beans.getBean().size());

        Bean bean = new Bean();
        bean.setName("tname");
        beans.getBean().add(bean);
        assertEquals(1,beans.getBean().size());
        assertSame(bean,beans.getBean().get(0));
    }

    @Test
    public void testEqualsObject() {
        assertTrue(beans.equals(beans2));
    }

    @Test
    public void testToString() {
        String expected = ToStringBuilder.reflectionToString(beans, ToStringStyle.MULTI_LINE_STYLE);
        assertEquals(expected,beans.toString());
    }

}
