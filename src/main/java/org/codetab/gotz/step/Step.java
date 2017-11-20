package org.codetab.gotz.step;

import javax.inject.Inject;

import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.XFieldHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
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
    private XField xField;
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
    protected XFieldHelper xFieldHelper;

    @Override
    public XField getXField() {
        return xField;
    }

    @Override
    public void setXField(final XField xField) {
        try {
            label = xFieldHelper.getLastValue("/:xfield/:label", xField);
        } catch (XFieldException e) {
        }
        this.xField = xField;
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
