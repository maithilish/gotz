package org.codetab.gotz.shared;

import org.codetab.gotz.step.IStep;

public enum StepService {

    INSTANCE;

    StepService() {
    }

    public IStep getStep(final String clzName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        IStep step = null;
        Class<?> stepClass = Class.forName(clzName);
        Object obj = stepClass.newInstance();
        if (obj instanceof IStep) {
            step = (IStep) obj;
        } else {
            throw new ClassCastException("Class " + clzName + " is not of type IStep");
        }
        return step;
    }

}
