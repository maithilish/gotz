package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

public class DAxisTest {

    /*
     * enhanced class to test excludes in hashcode and equals
     */
    class Enhanced extends DAxis {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    private DAxis dAxis;

    @Before
    public void setUp() throws Exception {
        dAxis = new DAxis();
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

        String expected = ToStringBuilder.reflectionToString(t1,
                ToStringStyle.MULTI_LINE_STYLE);
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetFields() {
        List<FieldsBase> fields = dAxis.getFields();
        assertThat(fields).isNotNull();

        // for test coverage when not null
        assertThat(dAxis.getFields()).isSameAs(fields);
    }

    @Test
    public void testGetMember() {
        Set<DMember> members = dAxis.getMember();

        assertThat(members).isNotNull();

        // for test coverage when not null
        assertThat(dAxis.getMember()).isSameAs(members);
    }

    // for coverage
    @Test
    public void testGetMemberNull() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(dAxis, "member", null, true);

        Set<DMember> members = dAxis.getMember();

        assertThat(members).isNotNull();
    }

    @Test
    public void testGetFilter() {
        DFilter filter = new DFilter();
        dAxis.setFilter(filter);

        assertThat(dAxis.getFilter()).isSameAs(filter);
    }

    private List<Enhanced> createTestObjects() {
        DFilter filter = new DFilter();
        filter.setName("f");

        Enhanced t1 = new Enhanced();
        t1.setId(1L);
        t1.setName("x");
        t1.setFilter(filter);

        Enhanced t2 = new Enhanced();
        t2.setId(2L);
        t2.setName("x");
        t2.setFilter(filter);
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
