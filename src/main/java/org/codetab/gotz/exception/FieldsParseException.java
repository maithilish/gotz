package org.codetab.gotz.exception;

/**
 * <p>
 * Exception thrown on field parse error.
 * <p>
 * unchecked exception : unrecoverable
 */
public class FieldsParseException extends RuntimeException {

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
    public FieldsParseException(final String message) {
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
    public FieldsParseException(final String message, final Throwable cause) {
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
    public FieldsParseException(final Throwable cause) {
        super(cause);
        this.message = null;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
