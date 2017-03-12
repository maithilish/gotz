package org.codetab.gotz.util;

import java.util.Iterator;

import org.codetab.gotz.model.FieldsBase;

public final class NullIterator implements Iterator<FieldsBase> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public FieldsBase next() {
        return null;
    }

}
