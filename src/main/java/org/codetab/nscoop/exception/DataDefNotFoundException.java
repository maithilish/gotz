package org.codetab.nscoop.exception;

public final class DataDefNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    private String name;

    public DataDefNotFoundException(final String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "[" + name + "]";
    }

}
