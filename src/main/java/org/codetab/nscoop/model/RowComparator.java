package org.codetab.nscoop.model;

import java.util.Comparator;

import org.codetab.nscoop.util.Util;

public class RowComparator implements Comparator<Member> {

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final Member m1, final Member m2) {
        Axis m1Row = m1.getAxis(AxisName.ROW);
        Axis m2Row = m2.getAxis(AxisName.ROW);
        if (Util.hasNulls(m1Row, m2Row)) {
            return 0;
        }
        return m1Row.getOrder() - m2Row.getOrder();
    }

}
