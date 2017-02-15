package in.m.picks.step;

import java.util.List;

import in.m.picks.model.FieldsBase;

public abstract class Seeder implements IStep {

	@Override
	public void run() {
		processStep();
	}

	// template method
	private void processStep() {
		try {
			load();
			handover();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void store() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setInput(Object input) {
	}

	@Override
	public void setFields(List<FieldsBase> fields) {
		// TODO Auto-generated method stub

	}

}
