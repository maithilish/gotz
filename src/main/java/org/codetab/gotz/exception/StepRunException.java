package org.codetab.gotz.exception;

/*
 * RuntimeException : unrecoverable
 */
public class StepRunException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;
    @SuppressWarnings("unused")
    private Throwable cause;

    public StepRunException(final String message) {
        super(message);
        this.message = message;
    }

    public StepRunException(final String message,final Throwable cause) {
        super(message,cause);
        this.message = message;
        this.cause = cause;
    }

    public StepRunException(final Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return "[" + message + "]";
    }
}
