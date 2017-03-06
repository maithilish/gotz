package in.m.picks.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.Data;
import in.m.picks.shared.MonitorService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Filter extends Step {

	final static Logger logger = LoggerFactory.getLogger(Filter.class);

	protected Data data;
	protected String locatorGroup;
	protected String locatorName;

	@Override
	public void run() {
		try {
			initialize();
			filter();
			consistent = true;
			handover();
		} catch (Exception e) {
			String message = "parse data " + Util.getLocatorLabel(fields);
			logger.error("{} {}", message, Util.getMessage(e));
			logger.debug("{}", e);
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
		}
	}

	protected abstract void filter() throws Exception;

	private void initialize() throws FieldNotFoundException {
		locatorName = FieldsUtil.getValue(fields, "locatorName");
		locatorGroup = FieldsUtil.getValue(fields, "locatorGroup");
	}

	@Override
	public boolean isConsistent() {
		return (consistent && data != null);
	}

	@Override
	public void handover() throws Exception {
		pushTask(data, fields);
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof Data) {
			this.data = (Data) input;
		} else {
			logger.error("input is not instance of Data {}",
					input.getClass().toString());
		}
	}

	@Override
	public void load() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void store() throws Exception {
		throw new UnsupportedOperationException();
	}

}
