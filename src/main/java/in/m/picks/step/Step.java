package in.m.picks.step;

import java.util.List;

import in.m.picks.model.FieldsBase;
import in.m.picks.shared.StepService;

public abstract class Step implements IStep {

	protected String stepName;

	protected IStep createTask(String taskClassName, Object input,
			List<FieldsBase> fields) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		IStep task = StepService.INSTANCE.getStep(taskClassName).instance();
		task.setInput(input);
		task.setFields(fields);
		return task;
	}
}
