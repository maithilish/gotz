package in.m.picks.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Activity.Type;
import in.m.picks.model.FieldComparator;
import in.m.picks.model.FieldsBase;
import in.m.picks.pool.TaskPoolService;
import in.m.picks.shared.MonitorService;
import in.m.picks.shared.StepService;
import in.m.picks.util.FieldsUtil;
import in.m.picks.util.Util;

public abstract class Step implements IStep {

	final static Logger logger = LoggerFactory.getLogger(Step.class);

	protected String label = "unknown";
	protected String stepType;
	protected boolean consistent = false;
	protected List<FieldsBase> fields;

	protected void pushTask(Object input, List<FieldsBase> nextStepFields) {
		String givenUpMessage = Util.buildString("[", label, "] step [", stepType,
				"] create next step failed");
		try {
			String nextStepType = getNextStepType(stepType);
			List<FieldsBase> stepTasks = getStepTaskFields(nextStepFields,
					nextStepType);
			if (stepTasks.size() == 0) {
				logger.warn("{}, no {} {}", givenUpMessage, nextStepType,
						"field found");
			}
			for (FieldsBase stepTask : stepTasks) {
				if (isConsistent()) {
					String stepClass = stepTask.getValue();
					IStep task = createTask(nextStepType, stepClass, input,
							nextStepFields);
					TaskPoolService.getInstance().submit(nextStepType, task);
					logger.debug("{} instance [{}] pushed to pool, entity [{}]",
							nextStepType, task.getClass(), label);
				} else {
					logger.warn("step inconsistent, entity [{}]", label);
					MonitorService.INSTANCE.addActivity(Type.GIVENUP,
							Util.buildString(givenUpMessage, ", step inconsistent"));
				}
			}
		} catch (Exception e) {
			logger.error("{}. {}", givenUpMessage, Util.getMessage(e));
			MonitorService.INSTANCE.addActivity(Type.GIVENUP, givenUpMessage, e);
		}
	}

	private List<FieldsBase> getStepTaskFields(List<FieldsBase> fields,
			String stepType) throws FieldNotFoundException {
		List<FieldsBase> list;
		try {
			list = FieldsUtil.getGroupFields(fields, "task");
		} catch (FieldNotFoundException e) {
			list = FieldsUtil.getGroupFields(fields, "datadef");
		}
		List<FieldsBase> stepTaskFields = FieldsUtil.getFieldList(list, stepType);
		return stepTaskFields;
	}

	private String getNextStepType(String stepType) throws FieldNotFoundException {
		List<FieldsBase> list = FieldsUtil.getGroupFields(fields, "steps");
		List<FieldsBase> steps = FieldsUtil.getFieldList(list);
		steps.sort(new FieldComparator());
		int order = FieldsUtil.getIntValue(steps, stepType);
		for (FieldsBase step : steps) {
			Integer nextOrder = Integer.valueOf(step.getValue());
			if (nextOrder > order) {
				return step.getName();
			}
		}
		throw new IllegalStateException("unable to get next step name");
	}

	@Override
	public void setFields(List<FieldsBase> fields) {
		this.fields = fields;
		try {
			label = FieldsUtil.getValue(fields, "label");
		} catch (FieldNotFoundException e1) {
		}
	}

	public void setStepType(String stepType) {
		this.stepType = stepType;
	}

	public String getStepType() {
		return stepType;
	}

	private IStep createTask(String stepType, String taskClassName, Object input,
			List<FieldsBase> fields) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		IStep task = StepService.INSTANCE.getStep(taskClassName).instance();
		task.setStepType(stepType);
		task.setInput(input);
		task.setFields(fields);
		return task;
	}

}
