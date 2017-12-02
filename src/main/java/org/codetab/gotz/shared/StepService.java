package org.codetab.gotz.shared;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.pool.TaskPoolService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.Step;
import org.codetab.gotz.step.Task;
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
    private FieldsHelper fieldsHelper;

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

    /**
     * Safely create instance from clzName string using DI. Steps should use
     * this method to create any objects such as converters etc.
     * @param clzName
     * @return
     * @throws ClassNotFoundException
     */
    public Object createInstance(final String clzName)
            throws ClassNotFoundException {
        Class<?> clz = Class.forName(clzName);
        return dInjector.instance(clz);
    }

    /**
     * Create task and assign step.
     * @param step
     *            to assign
     * @return task
     */
    public Task createTask(final IStep step) {
        Task task = dInjector.instance(Task.class);
        task.setStep(step);
        return task;
    }

    public void pushTask(final Step step, final Object input,
            final Labels labels, final Fields nextStepFields) {

        Validate.notNull(step, "step must not be null");
        Validate.notNull(input, "input must not be null");
        Validate.notNull(labels, "labels must not be null");
        Validate.notNull(nextStepFields, "nextStepFields must not be null");

        Validate.validState(step.getLabels() != null, "step labels not set");
        Validate.validState(step.getFields() != null, "step fields not set");

        String givenUpMessage = Util.buildString("[", step.getLabel(),
                "] step [", step.getStepType(), "] create next step failed");
        try {
            String nextStepType =
                    getNextStepType(step.getFields(), step.getStepType());

            if (nextStepType.equalsIgnoreCase("end")) {
                return;
            }

            List<String> stepClasses =
                    getNextStepClasses(nextStepFields, nextStepType);
            if (stepClasses.size() == 0) {
                LOGGER.warn("next step [{}], no stepClasses defined. {}",
                        nextStepType, givenUpMessage);
            }

            for (String stepClassName : stepClasses) {
                if (step.isConsistent()) {
                    Runnable task = null;
                    task = createTask(nextStepType, stepClassName, input,
                            labels, nextStepFields);
                    taskPoolService.submit(nextStepType, task);
                    LOGGER.debug("{} instance [{}] pushed to pool, entity [{}]",
                            nextStepType, task.getClass(), step.getLabel());
                } else {
                    LOGGER.warn("step inconsistent, entity [{}]",
                            step.getLabel());
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

    private List<String> getNextStepClasses(final Fields fields,
            final String stepType) throws FieldsException {

        String xpath = Util.buildString("//xf:task/xf:steps/xf:step[@name='",
                stepType, "']/@class");
        List<String> stepClasses = fieldsHelper.getValues(xpath, fields);

        // TODO handle unique step
        stepClasses =
                stepClasses.stream().distinct().collect(Collectors.toList());

        return stepClasses;
    }

    public String getNextStepType(final Fields fields, final String stepType)
            throws FieldsException {
        // TODO need to check behavior when multiple matching nodes exists
        String xpath = Util.buildString("//xf:task/xf:steps/xf:step[@name='",
                stepType, "']/xf:nextStep");
        String nextStepType = fieldsHelper.getFirstValue(xpath, fields);
        return nextStepType;
    }

    /**
     * Create step and assign it to task.
     *
     * @param stepType
     *            type
     * @param taskClassName
     *            task class
     * @param input
     *            task input
     * @param labels
     *            step labels
     * @param fields
     *            task fields
     * @return task
     * @throws ClassNotFoundException
     *             exception
     * @throws InstantiationException
     *             exception
     * @throws IllegalAccessException
     *             exception
     */
    private Task createTask(final String stepType, final String taskClassName,
            final Object input, final Labels labels, final Fields fields)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        IStep step = getStep(taskClassName).instance();
        step.setStepType(stepType);
        step.setInput(input);
        step.setFields(fields);
        step.setLabels(labels);
        Task task = createTask(step);
        return task;
    }
}
