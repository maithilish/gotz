package org.codetab.gotz.step;

import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.FieldComparator;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author m
 *
 */
public abstract class Step implements IStep {

    static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private String label = "unknown";
    private String stepType;
    private boolean consistent = false;
    private List<FieldsBase> fields;

    protected DataDefService dataDefService;
    protected ConfigService configService;
    protected StepService stepService;
    protected TaskPoolService taskPoolService;
    protected ActivityService activityService;

    @Inject
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Inject
    void setDataDefService(DataDefService dataDefService) {
        this.dataDefService = dataDefService;
    }

    @Inject
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Inject
    public void setStepService(StepService stepService) {
        this.stepService = stepService;
    }

    @Inject
    public void setTaskPoolService(TaskPoolService taskPoolService) {
        this.taskPoolService = taskPoolService;
    }

    /*
     *
     */
    protected void pushTask(final Object input, final List<FieldsBase> nextStepFields) {
        String givenUpMessage = Util.buildString("[", label, "] step [", stepType,
                "] create next step failed");
        try {
            String nextStepType = getNextStepType(stepType);
            List<FieldsBase> stepTasks = getStepTaskFields(nextStepFields, nextStepType);
            if (stepTasks.size() == 0) {
                LOGGER.warn("{}, no {} {}", givenUpMessage, nextStepType, "field found");
            }
            for (FieldsBase stepTask : stepTasks) {
                if (isConsistent()) {
                    String stepClass = stepTask.getValue();
                    IStep task = createTask(nextStepType, stepClass, input,
                            nextStepFields);
                    taskPoolService.submit(nextStepType, task);
                    LOGGER.debug("{} instance [{}] pushed to pool, entity [{}]",
                            nextStepType, task.getClass(), label);
                } else {
                    LOGGER.warn("step inconsistent, entity [{}]", label);
                    activityService.addActivity(Type.GIVENUP,
                            Util.buildString(givenUpMessage, ", step inconsistent"));
                }
            }
        } catch (Exception e) {
            LOGGER.error("{}. {}", givenUpMessage, Util.getMessage(e));
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
        }
    }

    private List<FieldsBase> getStepTaskFields(final List<FieldsBase> fields,
            final String stepType) throws FieldNotFoundException {
        List<FieldsBase> list;
        try {
            list = FieldsUtil.getGroupFields(fields, "task");
        } catch (FieldNotFoundException e) {
            list = FieldsUtil.getGroupFields(fields, "datadef");
        }
        List<FieldsBase> stepTaskFields = FieldsUtil.getFieldList(list, stepType);
        return stepTaskFields;
    }

    private String getNextStepType(final String stepType) throws FieldNotFoundException {
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
    public final void setFields(final List<FieldsBase> fields) {
        this.fields = fields;
        try {
            label = FieldsUtil.getValue(fields, "label");
        } catch (FieldNotFoundException e1) {
        }
    }

    /*
     *
     */
    public List<FieldsBase> getFields() {
        return fields;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return consistent;
    }

    /*
     *
     */
    public void setConsistent(final boolean consistent) {
        this.consistent = consistent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStep#setStepType(java.lang.String)
     */
    @Override
    public void setStepType(final String stepType) {
        this.stepType = stepType;
    }

    /**
     * @return stepType
     */
    public String getStepType() {
        return stepType;
    }

    /**
     * uses StepService to creates and returns task.
     *
     * @param stepType
     *            type
     * @param taskClassName
     *            task class
     * @param input
     *            task input
     * @param fields
     *            task fields
     * @return task
     * @throws ClassNotFoundException
     *             expection
     * @throws InstantiationException
     *             expection
     * @throws IllegalAccessException
     *             expection
     */
    private IStep createTask(final String stepType, final String taskClassName,
            final Object input, final List<FieldsBase> fields)
                    throws ClassNotFoundException, InstantiationException,
                    IllegalAccessException {
        IStep task = stepService.getStep(taskClassName).instance();
        task.setStepType(stepType);
        task.setInput(input);
        task.setFields(fields);
        return task;
    }

}
