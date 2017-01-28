package in.m.picks.appender;

public abstract class Appender implements Runnable {

	public enum Marker {
		EOF
	}

	abstract public void append(Object object) throws InterruptedException;
}
