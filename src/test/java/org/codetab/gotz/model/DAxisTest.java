package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;

public class DAxisTest {

    /*
     * required to test excludes in hashcode and equals
     */
    class EnhancedDAxis extends DAxis {
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
        List<EnhancedDAxis> list = createDAxis();
        EnhancedDAxis d1 = list.get(0);
        EnhancedDAxis d2 = list.get(1);

        String[] excludes = {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expectedHashD1 = HashCodeBuilder.reflectionHashCode(d1, excludes);
        int expectedHashD2 = HashCodeBuilder.reflectionHashCode(d2, excludes);

        assertThat(d1.hashCode()).isEqualTo(expectedHashD1);
        assertThat(d2.hashCode()).isEqualTo(expectedHashD2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<EnhancedDAxis> list = createDAxis();
        EnhancedDAxis d1 = list.get(0);
        EnhancedDAxis d2 = list.get(1);

        assertThat(d1).isEqualTo(d2);
        assertThat(d2).isEqualTo(d1);
    }

    @Test
    public void testToString() {
        List<EnhancedDAxis> list = createDAxis();
        EnhancedDAxis d1 = list.get(0);

        String expected = ToStringBuilder.reflectionToString(d1, ToStringStyle.MULTI_LINE_STYLE);
        assertThat(d1.toString()).isEqualTo(expected);
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
        FieldUtils.writeDeclaredField(dAxis, "member", null,true);

        Set<DMember> members = dAxis.getMember();

        assertThat(members).isNotNull();
    }

    @Test
    public void testGetFilter() {
        DFilter filter = new DFilter();
        dAxis.setFilter(filter);

        assertThat(dAxis.getFilter()).isSameAs(filter);
    }

    private List<EnhancedDAxis> createDAxis() {
        DFilter filter = new DFilter();
        filter.setName("f");

        EnhancedDAxis d1 = new EnhancedDAxis();
        d1.setId(1L);
        d1.setName("x");
        d1.setFilter(filter);

        EnhancedDAxis d2 = new EnhancedDAxis();
        d2.setId(2L);
        d2.setName("x");
        d2.setFilter(filter);
        d2.dnDetachedState = 11;
        d2.dnFlags = 12;
        d2.dnStateManager = 13;

        List<EnhancedDAxis> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);
        return list;
    }
}
