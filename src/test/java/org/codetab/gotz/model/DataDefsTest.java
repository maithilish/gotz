package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

public class DataDefsTest {

    // datadefs is just a holder and not compared with each other.
    // tests for hashCode, equals are for coverage
    @Test
    public void testHashCode() {
        DataDefs t1 = new DataDefs();
        DataDefs t2 = new DataDefs();
        t1.getDatadef();
        t2.getDatadef();

        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testGetDatadef() {
        DataDefs dataDefs = new DataDefs();
        List<DataDef> list = dataDefs.getDatadef();

        assertThat(list).isNotNull();
        // for coverage when not null
        assertThat(dataDefs.getDatadef()).isSameAs(list);
    }

    @Test
    public void testEqualsObject() {
        DataDefs t1 = new DataDefs();
        DataDefs t2 = new DataDefs();
        t1.getDatadef();
        t2.getDatadef();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        DataDefs d1 = new DataDefs();
        String expected = ToStringBuilder.reflectionToString(d1,
                ToStringStyle.MULTI_LINE_STYLE);

        assertThat(d1.toString()).isEqualTo(expected);
    }

}
