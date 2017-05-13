package org.codetab.gotz.exception;

/*
 * checked exception : recoverable, when config is not found then default value may be
 * used
 */
public class ConfigNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    private String key;

    public ConfigNotFoundException(String key) {
        this.key = key;
    }

    @Override
    public String getMessage() {
        return "[" + key + "]";
    }

}