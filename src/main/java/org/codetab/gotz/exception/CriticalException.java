package org.codetab.gotz.exception;

public class CriticalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;

    public CriticalException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[" + message + "]";
    }
}
