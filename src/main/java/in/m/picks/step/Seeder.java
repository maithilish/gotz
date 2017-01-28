package in.m.picks.step;

import in.m.picks.model.Afields;

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
	public void setAfields(Afields afields) {
	}

}
