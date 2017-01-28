package in.m.picks.exception;

public class AfieldNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private String name;

	public AfieldNotFoundException(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return "[" + name + "]";
	}

}
