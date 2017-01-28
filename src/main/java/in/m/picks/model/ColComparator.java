package in.m.picks.model;

import java.util.Comparator;

import in.m.picks.util.Util;

public class ColComparator implements Comparator<Member> {

	@Override
	public int compare(Member m1, Member m2) {
		Axis m1Col = m1.getAxis("col");
		Axis m2Col = m2.getAxis("col");
		if (Util.hasNulls(m1Col, m2Col))
			return 0;
		return m1Col.getOrder() - m2Col.getOrder();
	}

}
