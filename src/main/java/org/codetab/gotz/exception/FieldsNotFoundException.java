package org.codetab.gotz.exception;

/**
 * <p>
 * Exception thrown when field is not found.
 * <p>
 * checked exception : recoverable
 */
public class FieldsNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * message or xpath.
     */
    private final String message;

    /**
     * <p>
     * Constructor.
     * @param message
     *            config key
     */
    public FieldsNotFoundException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[" + message + "]";
    }

}
