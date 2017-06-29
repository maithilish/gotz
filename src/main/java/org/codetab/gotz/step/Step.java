package org.codetab.gotz.step;

import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.util.OFieldsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author m
 *
 */
public abstract class Step implements IStep {

    static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private String label = "not set";
    private String stepType;
    private boolean consistent = false;
    private List<FieldsBase> fields;
    private StepState stepState;

    //CHECKSTYLE:OFF
    @Inject
    protected DataDefService dataDefService;
    @Inject
    protected ConfigService configService;
    @Inject
    protected StepService stepService;
    @Inject
    protected ActivityService activityService;
    //CHECKSTYLE:ON


    @Override
    public final void setFields(final List<FieldsBase> fields) {
        this.fields = fields;
        try {
            label = OFieldsUtil.getValue(fields, "label");
        } catch (FieldNotFoundException e1) {
        }
    }

    /*
     *
     */
    @Override
    public List<FieldsBase> getFields() {
        return fields;
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
    public String getLabel() {
        return label;
    }
}
