package in.m.picks.poolold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Activity.Type;
import in.m.picks.model.Locator;
import in.m.picks.shared.MonitorService;

public class TransformerPool extends Pool {

	final Logger logger = LoggerFactory.getLogger(TransformerPool.class);

	public final static TransformerPool INSTANCE = new TransformerPool();

	private TransformerPool() {
		super("picks.transformerThreads");
	}

	public void givenupTask(Locator locator, Exception e) {
		MonitorService.INSTANCE.addActivity(Type.GIVENUP, locator.toString(), e);
	}

}
