package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

public class BeanTest {

    class Enhanced extends Bean {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int id = 5;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    private Bean bean;

    @Before
    public void setUp() throws Exception {
        bean = new Bean();
    }

    @Test
    public void testGetName() {
        bean.setName("x");
        assertThat(bean.getName()).isEqualTo("x");
    }

    @Test
    public void testGetXmlFile() {
        bean.setXmlFile("x");
        assertThat(bean.getXmlFile()).isEqualTo("x");
    }

    @Test
    public void testGetSchemaFile() {
        bean.setSchemaFile("x");
        assertThat(bean.getSchemaFile()).isEqualTo("x");
    }

    @Test
    public void testGetClassName() {
        bean.setPackageName("x");
        assertThat(bean.getPackageName()).isEqualTo("x");
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState",
                "dnFlags", "dnStateManager"};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState",
                "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected = ToStringBuilder.reflectionToString(t1,
                ToStringStyle.MULTI_LINE_STYLE);
        assertThat(t1.toString()).isEqualTo(expected);
    }

    private List<Enhanced> createTestObjects() {
        Enhanced t1 = new Enhanced();
        t1.setName("n");
        t1.setPackageName("c");
        t1.setSchemaFile("s");
        t1.setXmlFile("x");

        Enhanced t2 = new Enhanced();
        t2.setName("n");
        t2.setPackageName("c");
        t2.setSchemaFile("s");
        t2.setXmlFile("x");
        t2.id = 15;
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
