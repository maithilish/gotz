package org.codetab.gotz.model;

import java.util.Comparator;

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
        return m1Row.getOrder() - m2Row.getOrder();
    }

}
