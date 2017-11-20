package org.codetab.gotz.step;

import org.codetab.gotz.model.XField;

public interface IStep {

    IStep instance();

    boolean initialize();

    boolean load();

    boolean store();

    boolean process();

    boolean handover();

    boolean isConsistent();

    void setConsistent(boolean consistent);

    void setInput(Object input);

    void setStepType(String stepType);

    String getStepType();

    void setStepState(StepState stepState);

    StepState getStepState();

    String getLabel();

    XField getXField();

    void setXField(XField xField);
}
