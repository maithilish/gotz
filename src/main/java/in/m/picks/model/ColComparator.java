package in.m.picks.model;

import java.util.Comparator;

import in.m.picks.util.Util;

public final class ColComparator implements Comparator<Member> {

    @Override
    public int compare(final Member m1, final Member m2) {
        Axis m1Col = m1.getAxis(AxisName.COL);
        Axis m2Col = m2.getAxis(AxisName.COL);
        if (Util.hasNulls(m1Col, m2Col)) {
            return 0;
        }
        return m1Col.getOrder() - m2Col.getOrder();
    }

}
