package org.codetab.gotz.model;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Before;
import org.junit.Test;

public class ColComparatorTest {

    private Member m1, m2;
    private Axis axis1, axis2;

    private ColComparator sut;

    @Before
    public void setUp() throws Exception {
        m1 = new Member();
        m2 = new Member();

        axis1 = new Axis();
        axis1.setName(AxisName.COL);
        axis2 = new Axis();
        axis2.setName(AxisName.COL);

        m1.getAxes().add(axis1);
        m2.getAxes().add(axis2);

        sut = new ColComparator();
    }

    @Test
    public void testCompare() {
        axis1.setOrder(3);
        axis2.setOrder(1);

        int actual = sut.compare(m1, m2);

        then(actual).isEqualTo(2);
    }

}
