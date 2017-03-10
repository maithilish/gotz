package in.m.picks.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Activity.Type;
import in.m.picks.shared.MonitorService;

public abstract class Seeder extends Step {

	final static Logger logger = LoggerFactory.getLogger(Seeder.class);

	@Override
	public void run() {
		processStep();
	}

	// template method
	private void processStep() {
		try {
			load();
			consistent = true;
			handover();
		} catch (Exception e) {
			logger.warn("{}", e.getLocalizedMessage());
			logger.debug("{}", e);
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, "unable to seed",
					e);

		}
	}

	@Override
	public void store() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConsistent() {
		return consistent;
	}

	@Override
	public void setInput(Object input) {
	}

}
