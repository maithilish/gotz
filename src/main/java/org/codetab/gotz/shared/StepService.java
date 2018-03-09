package org.codetab.gotz.shared;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Validate;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.messages.Messages;
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
            throw new ClassCastException(Messages.getString("StepService.0") //$NON-NLS-1$
                    + clzName + Messages.getString("StepService.1")); //$NON-NLS-1$
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

        Validate.notNull(step, Messages.getString("StepService.2")); //$NON-NLS-1$
        Validate.notNull(input, Messages.getString("StepService.3")); //$NON-NLS-1$
        Validate.notNull(labels, Messages.getString("StepService.4")); //$NON-NLS-1$
        Validate.notNull(nextStepFields, Messages.getString("StepService.5")); //$NON-NLS-1$

        Validate.validState(step.getLabels() != null,
                Messages.getString("StepService.6")); //$NON-NLS-1$
        Validate.validState(step.getFields() != null,
                Messages.getString("StepService.7")); //$NON-NLS-1$

        String message = Util.join(Messages.getString("StepService.8"), //$NON-NLS-1$
                step.getStepType(), Messages.getString("StepService.9")); //$NON-NLS-1$

        try {
            if (!isTaskDefined(nextStepFields)) {
                message =
                        Util.join(message, Messages.getString("StepService.16"), //$NON-NLS-1$
                                labels.getGroup(), "]"); //$NON-NLS-1$
                throw new StepRunException(message);
            }

            String nextStepType =
                    getNextStepType(step.getFields(), step.getStepType());
            if (nextStepType
                    .equalsIgnoreCase(Messages.getString("StepService.10"))) { //$NON-NLS-1$
                return;
            }

            if (isNextStepDefined(nextStepFields, nextStepType)) {
                List<String> stepClasses =
                        getNextStepClasses(nextStepFields, nextStepType);
                if (stepClasses.size() == 0) {
                    message = Util.join(message,
                            Messages.getString("StepService.11"), //$NON-NLS-1$
                            nextStepType, "]"); //$NON-NLS-1$
                    throw new StepRunException(message);
                }

                for (String stepClassName : stepClasses) {
                    if (step.isConsistent()) {
                        Runnable task = null;
                        task = createTask(nextStepType, stepClassName, input,
                                labels, nextStepFields);
                        taskPoolService.submit(nextStepType, task);
                        LOGGER.debug(Messages.getString("StepService.13"), //$NON-NLS-1$
                                nextStepType, task.getClass(), step.getLabel());
                    } else {
                        message = Util.join(message,
                                Messages.getString("StepService.14")); //$NON-NLS-1$
                        throw new StepRunException(message);
                    }
                }
            } else {
                message =
                        Util.join(message, Messages.getString("StepService.15"), //$NON-NLS-1$
                                nextStepType, "]"); //$NON-NLS-1$
                throw new StepRunException(message);
            }
        } catch (FieldsNotFoundException | ClassNotFoundException
                | InstantiationException | IllegalAccessException e) {
            throw new StepRunException(message, e);
        }
    }

    private boolean isNextStepDefined(final Fields fields,
            final String stepType) {
        // xpath - not abs path
        String xpath =
                Util.join("//xf:task/xf:steps/xf:step[@name='", stepType, "']"); //$NON-NLS-1$ //$NON-NLS-2$
        return fieldsHelper.isDefined(xpath, true, fields);
    }

    private boolean isTaskDefined(final Fields fields) {
        // xpath - not abs path
        String xpath = Util.join("//xf:task"); //$NON-NLS-1$
        return fieldsHelper.isDefined(xpath, true, fields);
    }

    private List<String> getNextStepClasses(final Fields fields,
            final String stepType) throws FieldsNotFoundException {
        // xpath - not abs path
        String xpath = Util.join("//xf:task/xf:steps/xf:step[@name='", stepType, //$NON-NLS-1$
                "']/@class"); //$NON-NLS-1$
        List<String> stepClasses = fieldsHelper.getValues(xpath, false, fields);

        // TODO handle unique step
        stepClasses =
                stepClasses.stream().distinct().collect(Collectors.toList());

        return stepClasses;
    }

    public String getNextStepType(final Fields fields, final String stepType)
            throws FieldsNotFoundException {
        // TODO need to check behavior when multiple matching nodes exists
        // xpath - not abs path
        String xpath = Util.join("//xf:task/xf:steps/xf:step[@name='", stepType, //$NON-NLS-1$
                "']/xf:nextStep"); //$NON-NLS-1$
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
