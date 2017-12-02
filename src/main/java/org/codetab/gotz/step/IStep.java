package org.codetab.gotz.step;

import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;

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

    Fields getFields();

    void setFields(Fields fields);

    Labels getLabels();

    void setLabels(Labels labels);

    String getLabel();
}
