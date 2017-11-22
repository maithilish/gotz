package org.codetab.gotz.exception;

/**
 * <p>
 * Exception thrown when error accessing Fields.
 * <p>
 * RuntimeException : recoverable
 */
public class FieldsException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * message or xpath expression.
     */
    private final String message;
    /**
     * exception cause.
     */
    @SuppressWarnings("unused")
    private final Throwable cause;

    /**
     * <p>
     * Constructor.
     * @param message
     *            exception message
     */
    public FieldsException(final String message) {
        super(message);
        this.message = message;
        this.cause = null;
    }

    /**
     * <p>
     * Constructor.
     * @param message
     *            exception message
     * @param cause
     *            exception cause
     */
    public FieldsException(final String message, final Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    /**
     * <p>
     * Constructor.
     * @param cause
     *            exception cause
     */
    public FieldsException(final Throwable cause) {
        super(cause);
        this.message = null;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
