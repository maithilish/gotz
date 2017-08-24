package org.codetab.gotz.exception;

/**
 * <p>
 * Exception thrown when Field not found.
 * <p>
 * CheckedException : recoverable
 */
public final class FieldNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * field name.
     */
    private final String name;

    /**
     * <p>
     * Constructor.
     * @param name
     *            field name
     */
    public FieldNotFoundException(final String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "[" + name + "]";
    }

}
