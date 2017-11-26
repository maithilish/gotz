package org.codetab.gotz.model;

public final class Activity {

    public enum Type {
        GIVENUP, CONFIG, SUMMARY, FATAL, WARN
    };

    private Type type;
    private String message;
    private Throwable throwable;

    public Activity(final Type type, final String message) {
        super();
        this.type = type;
        this.message = message;
    }

    public Activity(final Type type, final String message,
            final Throwable throwable) {
        super();
        this.type = type;
        this.message = message;
        this.throwable = throwable;
    }

    public Type getType() {
        return type;
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
        sb.append("\n          message=");
        sb.append(message);
        if (throwable != null) {
            sb.append("\n          throwable=");
            sb.append(throwable);
        }
        sb.append("]");
        return sb.toString();
    }
}
