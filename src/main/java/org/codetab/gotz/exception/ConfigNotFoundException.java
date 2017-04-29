package org.codetab.gotz.exception;

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
