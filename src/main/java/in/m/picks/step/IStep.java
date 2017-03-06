package in.m.picks.step;

import java.util.List;

import in.m.picks.model.FieldsBase;

public interface IStep extends Runnable {

	IStep instance();

	void load() throws Exception;

	void store() throws Exception;

	void handover() throws Exception;

	void setInput(Object input);

	void setFields(List<FieldsBase> fields);

	void setStepType(String stepType);

	boolean isConsistent();

}
