package in.m.picks.model;

import java.util.Comparator;

import in.m.picks.util.Util;

public class RowComparator implements Comparator<Member> {

	@Override
	public int compare(Member m1, Member m2) {
		Axis m1Row = m1.getAxis("row");
		Axis m2Row = m2.getAxis("row");
		if (Util.hasNulls(m1Row, m2Row))
			return 0;
		return m1Row.getOrder() - m2Row.getOrder();
	}

}
