package in.m.picks.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Activity.Type;
import in.m.picks.model.FieldsBase;
import in.m.picks.shared.MonitorService;

public abstract class Seeder implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Seeder.class);
	
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
			logger.warn("{}", e.getLocalizedMessage());
			logger.trace("{}", e);			
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, "unable to seed", e);
					
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
	}
}
