package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codetab.gotz.util.FieldsIterator;
import org.junit.Before;
import org.junit.Test;

public class FieldsTest {

    // enhanced class to test excludes in hashcode and equals
    class Enhanced extends Fields {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    private Fields fields;

    @Before
    public void setUp() throws Exception {
        fields = new Fields();
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
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

        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected = ToStringBuilder.reflectionToString(t1, ToStringStyle.MULTI_LINE_STYLE);
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetName() {
        fields.setName("x");
        assertThat(fields.getName()).isEqualTo("x");
    }

    @Test
    public void testGetValue() {
        fields.setValue("x");
        assertThat(fields.getValue()).isEqualTo("x");
    }

    @Test
    public void testGetFields() {
        List<FieldsBase> fieldsList = fields.getFields();
        assertThat(fieldsList).isNotNull();

        // for test coverage when not null
        assertThat(fields.getFields()).isSameAs(fieldsList);
    }

    @Test
    public void testIterator() {
        fields.getFields();
        Iterator<FieldsBase> i1 = fields.iterator();
        Iterator<FieldsBase> i2 = fields.iterator();
        assertThat(i1).isInstanceOf(FieldsIterator.class);
        assertThat(i2).isInstanceOf(FieldsIterator.class);
        assertThat(i1).isNotSameAs(i2);
    }

    private List<Enhanced> createTestObjects() {
        Enhanced t1 = new Enhanced();
        t1.setName("x");
        t1.setValue("v");

        Enhanced t2 = new Enhanced();
        t2.setName("x");
        t2.setValue("v");
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
