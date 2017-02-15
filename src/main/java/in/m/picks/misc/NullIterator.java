package in.m.picks.misc;

import java.util.Iterator;

import in.m.picks.model.FieldsBase;

public class NullIterator implements Iterator<FieldsBase> {

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public FieldsBase next() {
		return null;
	}

}
