package in.m.picks.exception;

public class FieldNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private String name;

	public FieldNotFoundException(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return "[" + name + "]";
	}

}
