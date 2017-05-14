package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

public class DataDefsTest {

    // datadefs are just holder and not compared
    // tests for hashCode, equals are for coverage

    @Test
    public void testHashCode() {
        DataDefs d1 = new DataDefs();
        DataDefs d2 = new DataDefs();
        d1.getDatadef();
        d2.getDatadef();

        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
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
        DataDefs d1 = new DataDefs();
        DataDefs d2 = new DataDefs();
        d1.getDatadef();
        d2.getDatadef();

        assertThat(d1).isEqualTo(d2);
        assertThat(d2).isEqualTo(d1);
    }

    @Test
    public void testToString() {
        DataDefs d1 = new DataDefs();
        String expected = ToStringBuilder.reflectionToString(d1,
                ToStringStyle.MULTI_LINE_STYLE);

        assertThat(d1.toString()).isEqualTo(expected);
    }

}
