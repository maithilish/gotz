package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

public class DataDefTest {

    private DataDef dataDef;

    class EnhancedDataDef extends DataDef{
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    @Before
    public void setUp() throws Exception {
        dataDef = new DataDef();
    }

    @Test
    public void testGetFields() {
        List<FieldsBase> fields = dataDef.getFields();
        assertThat(fields).isNotNull();

        // for test coverage when not null
        assertThat(dataDef.getFields()).isSameAs(fields);
    }

    @Test
    public void testGetAxis() {
        List<DAxis> axes = dataDef.getAxis();
        assertThat(axes).isNotNull();

        // for test coverage when not null
        assertThat(dataDef.getAxis()).isSameAs(axes);
    }

    @Test
    public void testGetFromDate() {
        Date date = new Date();
        dataDef.setFromDate(date);

        assertThat(dataDef.getFromDate()).isEqualTo(date);
    }

    @Test
    public void testGetToDate() {
        Date date = new Date();
        dataDef.setToDate(date);

        assertThat(dataDef.getToDate()).isEqualTo(date);

    }

    @Test
    public void testHashCode() {
        List<EnhancedDataDef> dataDefs = createDataDefs();
        EnhancedDataDef d1 = dataDefs.get(0);
        EnhancedDataDef d2 = dataDefs.get(0);

        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState", "dnFlags",
        "dnStateManager"};
        int expectedHashD1 = HashCodeBuilder.reflectionHashCode(d1, excludes);
        int expectedHashD2 = HashCodeBuilder.reflectionHashCode(d2, excludes);

        assertThat(d1.hashCode()).isEqualTo(expectedHashD1);
        assertThat(d2.hashCode()).isEqualTo(expectedHashD2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    public void testEqualsSymetry() {
        List<EnhancedDataDef> dataDefs = createDataDefs();
        EnhancedDataDef d1 = dataDefs.get(0);
        EnhancedDataDef d2 = dataDefs.get(0);

        assertThat(d1).isEqualTo(d2);
        assertThat(d2).isEqualTo(d1);
    }

    @Test
    public void testToString() {
        List<EnhancedDataDef> dataDefs = createDataDefs();
        EnhancedDataDef d1 = dataDefs.get(0);
        String expected = ToStringBuilder.reflectionToString(d1, ToStringStyle.MULTI_LINE_STYLE);
        assertThat(d1.toString()).isEqualTo(expected);
    }

    private List<EnhancedDataDef> createDataDefs(){
        Date date = new Date();

        EnhancedDataDef d1 = new EnhancedDataDef();
        d1.setFromDate(date);
        d1.setToDate(DateUtils.addMonths(date, 1));
        d1.setId(1L);
        d1.setName("x");

        EnhancedDataDef d2 = new EnhancedDataDef();

        d2.setFromDate(DateUtils.addMonths(date, 2));
        d2.setToDate(DateUtils.addMonths(date, 3));
        d2.setId(2L);
        d2.setName("x");
        d2.dnDetachedState=11;
        d2.dnFlags=12;
        d2.dnStateManager=13;

        List<EnhancedDataDef> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);
        return list;
    }
}
