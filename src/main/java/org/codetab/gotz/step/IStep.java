package org.codetab.gotz.step;

import java.util.List;

import org.codetab.gotz.model.FieldsBase;

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

    void setFields(List<FieldsBase> fields);

    List<FieldsBase> getFields();

    String getLabel();
}
