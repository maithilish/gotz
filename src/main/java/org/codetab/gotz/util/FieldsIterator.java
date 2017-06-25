package org.codetab.gotz.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;

public final class FieldsIterator implements Iterator<FieldsBase> {

    private final Stack<FieldsBase> stack = new Stack<>();

    public FieldsIterator(final List<FieldsBase> fieldsBaseList) {
        for (FieldsBase fieldBase : fieldsBaseList) {
            stack.push(fieldBase);
        }
    }

    public FieldsIterator(final FieldsBase fieldsBase) {
        stack.push(fieldsBase);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    //    @Override
    //    public FieldsBase next() {
    //        if (stack.isEmpty()) {
    //            throw new NoSuchElementException();
    //        }
    //        FieldsBase nextFb = stack.pop();
    //        if (nextFb != null) {
    //            if (nextFb instanceof Fields) {
    //                Fields fields = (Fields) nextFb;
    //                for (FieldsBase nestedFb : fields.getFields()) {
    //                    stack.add(nestedFb);
    //                }
    //            }
    //        }
    //        return nextFb;
    //    }

    @Override
    public FieldsBase next() {
        if (stack.isEmpty()) {
            throw new NoSuchElementException();
        }
        FieldsBase nextFb = stack.pop();
        if (nextFb != null) {
            if (nextFb instanceof Fields) {
                Fields fields = (Fields) nextFb;
                List<FieldsBase> fieldsFields = fields.getFields();
                for (int i = fieldsFields.size() - 1; i >= 0; i--) {
                    FieldsBase nestedFb = fieldsFields.get(i);
                    stack.push(nestedFb);
                }
            }
        }
        return nextFb;
    }
}
