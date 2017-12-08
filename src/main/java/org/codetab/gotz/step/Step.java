package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author m
 *
 */
public abstract class Step implements IStep {

    static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private Labels labels;
    private String stepType;
    private boolean consistent = false;
    private Fields fields;
    private StepState stepState;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected DataDefService dataDefService;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ConfigService configService;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected StepService stepService;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ActivityService activityService;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected FieldsHelper fieldsHelper;

    @Override
    public Fields getFields() {
        return fields;
    }

    @Override
    public void setFields(final Fields fields) {
        this.fields = fields;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        return consistent;
    }

    /*
     *
     */
    @Override
    public void setConsistent(final boolean consistent) {
        this.consistent = consistent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codetab.gotz.step.IStepO#setStepType(java.lang.String)
     */
    @Override
    public void setStepType(final String stepType) {
        this.stepType = stepType;
    }

    /**
     * @return stepType
     */
    @Override
    public String getStepType() {
        return stepType;
    }

    @Override
    public void setStepState(final StepState stepState) {
        this.stepState = stepState;
    }

    @Override
    public StepState getStepState() {
        return stepState;
    }

    @Override
    public Labels getLabels() {
        return labels;
    }

    @Override
    public void setLabels(final Labels labels) {
        this.labels = labels;
    }

    /**
     * get label with []
     */
    @Override
    public String getLabel() {
        return labels.getLabel();
    }

    @Override
    public String getLabeled(final String message) {
        return Util.join("[", labels.getLabel(), "] ", message);
    }

}
