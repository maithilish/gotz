package org.codetab.gotz.exception;

/*
 * RuntimeException : unrecoverable
 */
public class StepPersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;
    @SuppressWarnings("unused")
    private Throwable cause;

    public StepPersistenceException(final String message) {
        super(message);
        this.message = message;
    }

    public StepPersistenceException(final String message,final Throwable cause) {
        super(message,cause);
        this.message = message;
        this.cause = cause;
    }

    public StepPersistenceException(final Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return "[" + message + "]";
    }
}
