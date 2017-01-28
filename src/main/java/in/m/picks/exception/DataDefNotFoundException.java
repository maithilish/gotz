package in.m.picks.exception;

public class DataDefNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private String name;

	public DataDefNotFoundException(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return "[" + name + "]";
	}

}
