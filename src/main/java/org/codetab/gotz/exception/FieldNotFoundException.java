package org.codetab.gotz.exception;

/*
 * checked exception : recoverable, when field is not found then default value may be
 * used
 */
public final class FieldNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    private String name;

    public FieldNotFoundException(final String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "[" + name + "]";
    }

}
