package in.m.picks.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import in.m.picks.model.Fields;
import in.m.picks.model.FieldsBase;

public class FieldsIterator implements Iterator<FieldsBase> {

	final private Stack<FieldsBase> stack = new Stack<>();

	public FieldsIterator(List<FieldsBase> fieldsBaseList) {
		for (FieldsBase fieldBase : fieldsBaseList) {
			stack.push(fieldBase);
		}
	}

	public FieldsIterator(FieldsBase fieldsBase) {
		stack.push(fieldsBase);
	}

	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public FieldsBase next() {
		if (stack.isEmpty()) {
			throw new NoSuchElementException();
		}
		FieldsBase nextFb = stack.pop();
		if (nextFb != null) {
			if (nextFb instanceof Fields) {
				Fields fields = (Fields) nextFb;
				for (FieldsBase nestedFb : fields.getFields()) {
					stack.add(nestedFb);
				}
			}
		}
		return nextFb;
	}
}
