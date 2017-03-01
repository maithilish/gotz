package in.m.picks.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.FieldsBase;
import in.m.picks.shared.MonitorService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Filter extends Step {

	final static Logger logger = LoggerFactory.getLogger(Filter.class);
	protected List<FieldsBase> fields;
	protected String locatorName;

	@Override
	public void run() {
		try {
			initialize();
			filter();
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
	}

	@Override
	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
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
