package in.m.picks.model;

public class Activity {

	public enum Type {
		GIVENUP, CONFIG, SUMMARY, FATAL
	};

	private Type type;
	private String message;
	private Throwable throwable;

	public Activity(Type type, String message) {
		super();
		this.type = type;
		this.message = message;
	}

	public Activity(Type type, String message, Throwable throwable) {
		super();
		this.type = type;
		this.message = message;
		this.throwable = throwable;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
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
