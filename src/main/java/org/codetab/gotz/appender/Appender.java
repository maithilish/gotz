package org.codetab.gotz.appender;

import java.util.List;

import org.codetab.gotz.model.FieldsBase;

public abstract class Appender implements Runnable {

    private List<FieldsBase> fields;

    public enum Marker {
        EOF
    }

    public abstract void append(Object object) throws InterruptedException;

    /*
     *
     */
    public void setFields(final List<FieldsBase> fields) {
        this.fields = fields;
    }

    /*
     *
     */
    public List<FieldsBase> getFields() {
        return fields;
    }

}
