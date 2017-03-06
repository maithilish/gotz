package in.m.picks.appender;

import java.util.List;

import in.m.picks.model.FieldsBase;

public abstract class Appender implements Runnable {

	protected List<FieldsBase> fields;

	public enum Marker {
		EOF
	}

	abstract public void append(Object object) throws InterruptedException;

	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
	}
}
