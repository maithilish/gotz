package in.m.picks.model;

import java.util.Comparator;

public class FieldComparator implements Comparator<FieldsBase> {

	@Override
	public int compare(FieldsBase f1, FieldsBase f2) {
		Integer v1 = Integer.valueOf(f1.getValue());
		Integer v2 = Integer.valueOf(f2.getValue());
		return v1 - v2;
	}

}
