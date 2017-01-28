package in.m.picks.step;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Afields;
import in.m.picks.model.Data;

public abstract class Transformer implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Transformer.class); 
	
	protected Data data;
	protected Afields afields;

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
			e.printStackTrace();
		}
	}

	protected abstract void transform() throws Exception;
	

	@Override
	public void load() throws Exception {
		throw new OperationNotSupportedException("Nothing to load");
	}

	@Override
	public void store() throws Exception {
		throw new OperationNotSupportedException("Nothing to store");
	}

	@Override
	public void setInput(Object input) {
		if(input instanceof Data){
			data = (Data) input;
		}else {
			logger.warn("Input is not instance of Data type. {}",
					input.getClass().toString());
		}
	}

	@Override
	public void setAfields(Afields afields) {
		this.afields = afields;
	}

}
