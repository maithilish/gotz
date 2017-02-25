package in.m.picks.step;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Activity.Type;
import in.m.picks.model.Data;
import in.m.picks.model.FieldsBase;
import in.m.picks.shared.MonitorService;
import in.m.picks.util.Util;

public abstract class Transformer implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Transformer.class);

	protected Data data;
	protected List<FieldsBase> fields;

	@Override
	public void run() {
		processStep();
	}

	// template method
	private void processStep() {
		try {
			transform();
			handover();
		} catch (Exception e) {
			String message = "transform data " + Util.getLocatorLabel(fields);
			logger.error("{} {}", message, Util.getMessage(e));
			logger.debug("{}", e);
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, message, e);
		}
	}

	protected abstract void transform() throws Exception;

	@Override
	public void load() throws Exception {
		throw new OperationNotSupportedException("nothing to load");
	}

	@Override
	public void store() throws Exception {
		throw new OperationNotSupportedException("nothing to store");
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof Data) {
			data = (Data) input;
		} else {
			logger.error("input is not instance of Data type. {}",
					input.getClass().toString());
		}
	}

	@Override
	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
	}

}
