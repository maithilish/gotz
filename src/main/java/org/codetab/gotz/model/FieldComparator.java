package org.codetab.gotz.model;

import java.util.Comparator;

public final class FieldComparator implements Comparator<FieldsBase> {

    @Override
    public int compare(final FieldsBase f1, final FieldsBase f2) {
        Integer v1 = Integer.valueOf(f1.getValue());
        Integer v2 = Integer.valueOf(f2.getValue());
        return v1 - v2;
    }

}
