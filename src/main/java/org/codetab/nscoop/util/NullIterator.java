package org.codetab.nscoop.util;

import java.util.Iterator;

import org.codetab.nscoop.model.FieldsBase;

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
