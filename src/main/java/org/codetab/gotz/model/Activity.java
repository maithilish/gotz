package org.codetab.gotz.model;

import org.codetab.gotz.util.Util;

public final class Activity {

    public enum Type {
        FAIL, CONFIG, SUMMARY, FATAL, WARN, INTERNAL
    };

    private Type type;
    private String label;
    private String message;
    private Throwable throwable;

    // TODO remove this
    public Activity(final Type type, final String message) {
        super();
        this.type = type;
        this.message = message;
    }

    // TODO remove this
    public Activity(final Type type, final String message,
            final Throwable throwable) {
        this.type = type;
        this.message = message;
        this.throwable = throwable;
    }

    public Activity(final Type type, final String label, final String message) {
        this.type = type;
        this.message = message;
        this.label = label;
    }

    public Activity(final Type type, final String label, final String message,
            final Throwable throwable) {
        this.type = type;
        this.message = message;
        this.label = label;
        this.throwable = throwable;
    }

    public Type getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Activity [type=");
        sb.append(type);
        sb.append(" label=");
        sb.append(label);
        sb.append(" message=");
        sb.append(message);
        sb.append("]");
        if (throwable != null) {
            sb.append(Util.LINE);
            sb.append("          throwable=");
            sb.append(throwable);
        }
        return sb.toString();
    }
}
