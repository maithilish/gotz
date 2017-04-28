package org.codetab.gotz.exception;

public class FatalException extends Exception {

    private static final long serialVersionUID = 1L;

    private String message;

    public FatalException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[" + message + "]";
    }
}
