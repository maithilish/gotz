package org.codetab.gotz.model.iterator;

import java.util.Iterator;

import org.codetab.gotz.model.FieldsBase;

/**
 * <p>
 * Null iterator.
 * @author Maithilish
 *
 */
public final class NullIterator implements Iterator<FieldsBase> {

    /**
     * @return false
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * @return null
     */
    @Override
    public FieldsBase next() {
        return null;
    }

}
