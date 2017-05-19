package org.codetab.gotz.step;

import java.util.List;

import org.codetab.gotz.model.FieldsBase;

public interface IStepO extends Runnable {

    IStepO instance();

    void load();

    void store();

    void handover();

    void setInput(Object input);

    void setFields(List<FieldsBase> fields);

    void setStepType(String stepType);

    boolean isConsistent();

}
