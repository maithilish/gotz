package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codetab.gotz.util.NullIterator;
import org.junit.Before;
import org.junit.Test;

public class FieldTest {

    // enhanced class to test excludes in hashcode and equals
    class Enhanced extends Field {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    private Field field;

    @Before
    public void setUp() throws Exception {
        field = new Field();
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
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

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected = new StringBuilder().append(System.lineSeparator())
                .append("   ").append("- {name: ").append(t1.getName())
                .append(", value: ").append(t1.getValue()).append("}")
                .toString();
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetName() {
        field.setName("x");
        assertThat(field.getName()).isEqualTo("x");
    }

    @Test
    public void testGetValue() {
        field.setValue("x");
        assertThat(field.getValue()).isEqualTo("x");
    }

    @Test
    public void testIterator() {
        Iterator<FieldsBase> i1 = field.iterator();
        Iterator<FieldsBase> i2 = field.iterator();
        assertThat(i1).isInstanceOf(NullIterator.class);
        assertThat(i2).isInstanceOf(NullIterator.class);
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
