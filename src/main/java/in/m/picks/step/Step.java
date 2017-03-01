package in.m.picks.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Activity.Type;
import in.m.picks.model.FieldsBase;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Step implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Step.class);

	protected String entityLabel;
	protected String nextStepType;
	protected boolean consistent = false;

	protected void pushTask(Object input, List<FieldsBase> fields) {
		String givenUpMessage = Util.buildString("create ", nextStepType,
				" for entity [", entityLabel, "] failed");
		try {
			List<FieldsBase> stepClasses = FieldsUtil.getFieldList(fields,
					nextStepType);
			if (stepClasses.size() == 0) {
				logger.warn("{}, no {} {}", givenUpMessage, nextStepType,
						"field found");
			}
			for (FieldsBase stepClassField : stepClasses) {
				if (isConsistent()) {
					String stepClass = stepClassField.getValue();
					IStep task = createTask(stepClass, input, fields);
					TaskPoolService.getInstance().submit(nextStepType, task);
					logger.debug("{} instance [{}] pushed to pool, entity [{}]",
							nextStepType, task.getClass(), entityLabel);
				} else {
					logger.warn("step inconsistent, entity [{}]", entityLabel);
					MonitorService.INSTANCE.addActivity(Type.GIVENUP,
							Util.buildString(givenUpMessage, ", step inconsistent"));
				}
			}
		} catch (Exception e) {
			logger.error("{}. {}", givenUpMessage, Util.getMessage(e));
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, givenUpMessage, e);
		}
	}

	private IStep createTask(String taskClassName, Object input,
			List<FieldsBase> fields) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		IStep task = StepService.INSTANCE.getStep(taskClassName).instance();
		task.setInput(input);
		task.setFields(fields);
		return task;
	}
}
