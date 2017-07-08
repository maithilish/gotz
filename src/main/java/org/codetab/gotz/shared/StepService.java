package org.codetab.gotz.shared;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.Task;
import org.codetab.gotz.util.FieldsUtil;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StepService {

    static final Logger LOGGER = LoggerFactory.getLogger(StepService.class);

    @Inject
    private DInjector dInjector;
    @Inject
    private ActivityService activityService;
    @Inject
    private TaskPoolService taskPoolService;

    @Inject
    private StepService() {
    }

    public IStep getStep(final String clzName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        IStep step = null;
        Class<?> stepClass = Class.forName(clzName);
        Object obj = dInjector.instance(stepClass);
        if (obj instanceof IStep) {
            step = (IStep) obj;
        } else {
            throw new ClassCastException(
                    "Class " + clzName + " is not of type IStep");
        }
        return step;
    }

    public Task createTask(final IStep step) {
        Task task = dInjector.instance(Task.class);
        task.setStep(step);
        return task;
    }

    public void pushTask(final Step step, final Object input,
            final List<FieldsBase> nextStepFields) {
        String label = step.getLabel();
        addLabelField(step, nextStepFields);

        String givenUpMessage = Util.buildString("[", label, "] step [",
                step.getStepType(), "] create next step failed");
        try {
            String nextStepType =
                    getNextStepType(step.getFields(), step.getStepType());
            List<FieldsBase> stepClasses =
                    getNextStepClasses(nextStepFields, nextStepType);
            if (stepClasses.size() == 0) {
                LOGGER.warn("{}, no {} {}", givenUpMessage, nextStepType,
                        "field found");
            }
            for (FieldsBase stepClassField : stepClasses) {
                if (step.isConsistent()) {
                    String stepClassName = stepClassField.getValue();
                    Runnable task = null;
                    task = createTask(nextStepType, stepClassName, input,
                            nextStepFields);
                    taskPoolService.submit(nextStepType, task);
                    LOGGER.debug("{} instance [{}] pushed to pool, entity [{}]",
                            nextStepType, task.getClass(), label);
                } else {
                    LOGGER.warn("step inconsistent, entity [{}]", label);
                    activityService.addActivity(Type.GIVENUP, Util.buildString(
                            givenUpMessage, ", step inconsistent"));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("{}", e);
            LOGGER.error("{}. {}", givenUpMessage, Util.getMessage(e));
            activityService.addActivity(Type.GIVENUP, givenUpMessage, e);
        }
    }

    private void addLabelField(final Step step,
            final List<FieldsBase> nextStepFields) {
        if (FieldsUtil.isDefined(nextStepFields, "label")) {
            return;
        }
        String label = step.getLabel();
        if (label == null) {
            LOGGER.warn("unable to add label field to next step");
            return;
        }
        nextStepFields.add(FieldsUtil.createField("label", label));
    }

    private List<FieldsBase> getNextStepClasses(final List<FieldsBase> fields,
            final String stepType) throws FieldNotFoundException {
        List<FieldsBase> list;
        try {
            list = FieldsUtil.filterByGroup(fields, "task");
        } catch (FieldNotFoundException e) {
            list = FieldsUtil.filterByGroup(fields, "datadef");
        }
        List<FieldsBase> step =
                FieldsUtil.filterByValue(list, "step", stepType);
        List<FieldsBase> stepClasses =
                FieldsUtil.filterChildrenByName(step, "class");
        try {
            FieldsUtil.getField(step, "unique");
            stepClasses = stepClasses.stream().distinct()
                    .collect(Collectors.toList());
        } catch (FieldNotFoundException e) {
        }
        return stepClasses;
    }

    public String getNextStepType(final List<FieldsBase> fields,
            final String stepType) throws FieldNotFoundException {
        List<FieldsBase> step =
                FieldsUtil.filterByValue(fields, "step", stepType);
        return FieldsUtil.getValue(step, "nextstep");
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
    private Task createTask(final String stepType, final String taskClassName,
            final Object input, final List<FieldsBase> fields)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        IStep step = getStep(taskClassName).instance();
        step.setStepType(stepType);
        step.setInput(input);
        step.setFields(fields);
        Task task = createTask(step);
        return task;
    }
}
