package org.codetab.gotz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

public class BeanTest {

    class EnhancedBean extends Bean{
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    private EnhancedBean bean;
    private EnhancedBean bean2;

    @Before
    public void setUp() throws Exception {
        bean = new EnhancedBean();
        bean.setName("tname");
        bean.setSchemaFile("tschema");
        bean.setXmlFile("txml");
        bean.setClassName("tclass");

        bean2 = new EnhancedBean();
        bean2.setName("tname");
        bean2.setSchemaFile("tschema");
        bean2.setXmlFile("txml");
        bean2.setClassName("tclass");
        bean2.dnFlags = 21;
        bean2.dnDetachedState = 22;
        bean2.dnStateManager = 23;
    }

    @Test
    public void testBean() {
        assertEquals("tname",bean.getName());
        assertEquals("tschema",bean.getSchemaFile());
        assertEquals("txml",bean.getXmlFile());
        assertEquals("tclass",bean.getClassName());
    }

    @Test
    public void testHashCode() {
        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expected = HashCodeBuilder.reflectionHashCode(bean, excludes);

        assertEquals(expected, bean.hashCode());
        assertEquals(bean.hashCode(),bean2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        assertTrue(bean.equals(bean2));
        assertTrue(bean2.equals(bean2));
    }

    @Test
    public void testToString() {
        String expected = ToStringBuilder.reflectionToString(bean, ToStringStyle.MULTI_LINE_STYLE);
        assertEquals(expected, bean.toString());
    }

}
