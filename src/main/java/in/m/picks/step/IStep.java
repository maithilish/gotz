package in.m.picks.step;

import in.m.picks.model.Afields;

public interface IStep extends Runnable {

	IStep instance();

	void load() throws Exception;

	void store() throws Exception;

	void handover() throws Exception;

	void setInput(Object input);

	void setAfields(Afields afields);

}
