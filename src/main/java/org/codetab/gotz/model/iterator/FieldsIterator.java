package org.codetab.gotz.model.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;

/**
 * <p>
 * FieldsBase iterator. Recursively return items from FieldsBase includes all
 * child elements of Fields.
 * @author Maithilish
 *
 */
public final class FieldsIterator implements Iterator<FieldsBase> {

    /**
     * stack.
     */
    private final Stack<FieldsBase> stack = new Stack<>();

    /**
     * <p>
     * Push FieldsBase items to stack from the list in reverse order.
     * @param fieldsBaseList
     *            list of items
     */
    public FieldsIterator(final List<FieldsBase> fieldsBaseList) {

        Validate.notNull(fieldsBaseList, "fieldsBaseList must not be null");

        for (int i = fieldsBaseList.size() - 1; i >= 0; i--) {
            FieldsBase fb = fieldsBaseList.get(i);
            stack.push(fb);
        }
    }

    /**
     * <p>
     * Push FieldsBase to stack.
     * @param fieldsBase
     *            item
     */
    public FieldsIterator(final FieldsBase fieldsBase) {

        Validate.notNull(fieldsBase, "fieldsBase must not be null");

        stack.push(fieldsBase);
    }

    /**
     * @return true if has next
     */
    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    /**
     * Get next item.
     * <p>
     * It returns element at each level and returned element will contain all
     * its children. For example, following fields has four level
     *
     * <pre>
     *
     *  (1) - fields: {name: g1, value: g1v}
     *        - fields: {name: fs1, value: fs1v}
     *           - {name: f1, value: f1v}
     *        - {name: f2, value: f2v}
     *
     *  and next() will return four items, each with its child elements.
     *
     *  (1) - fields: {name: g1, value: g1v}
     *        - fields: {name: fs1, value: fs1v}
     *           - {name: f1, value: f1v}
     *        - {name: f2, value: f2v},
     *
     *  (2) - fields: {name: fs1, value: fs1v}
     *        - {name: f1, value: f1v},
     *
     *  (3) - {name: f1, value: f1v},
     *
     *  (4) - {name: f2, value: f2v}
     *
     * </pre>
     *
     * @return next fieldsBase item from stack
     * @throws NoSuchElementException
     *             if stack is empty
     */
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
