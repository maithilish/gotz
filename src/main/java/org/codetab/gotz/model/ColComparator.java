package org.codetab.gotz.model;

import java.util.Comparator;

public final class ColComparator implements Comparator<Member> {

    @Override
    public int compare(final Member m1, final Member m2) {
        Axis m1Col = m1.getAxis(AxisName.COL);
        Axis m2Col = m2.getAxis(AxisName.COL);
        return m1Col.getOrder() - m2Col.getOrder();
    }

}
