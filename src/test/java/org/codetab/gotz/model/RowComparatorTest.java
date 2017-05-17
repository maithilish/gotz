package org.codetab.gotz.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RowComparatorTest {

    @Test
    public void testCompare() {
        Axis r1 = new Axis();
        r1.setName(AxisName.ROW);
        Member m1 = new Member();
        m1.addAxis(r1);

        Axis r2 = new Axis();
        r2.setName(AxisName.ROW);
        Member m2 = new Member();
        m2.addAxis(r2);

        RowComparator rc = new RowComparator();

        r1.setOrder(1);
        r2.setOrder(1);
        assertThat(rc.compare(m1, m2)).isEqualTo(0);

        r1.setOrder(2);
        r2.setOrder(1);
        assertThat(rc.compare(m1, m2)).isEqualTo(1);

        r1.setOrder(1);
        r2.setOrder(2);
        assertThat(rc.compare(m1, m2)).isEqualTo(-1);
    }
}
