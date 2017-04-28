package org.codetab.gotz.shared;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.step.IStep;

@Singleton
public class StepService {

    private DInjector dInjector;

    @Inject
    private StepService() {
    }

    @Inject
    public void setdInjector(DInjector dInjector) {
        this.dInjector = dInjector;
    }

    public IStep getStep(final String clzName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        IStep step = null;
        Class<?> stepClass = Class.forName(clzName);
        Object obj = dInjector.instance(stepClass);
        if (obj instanceof IStep) {
            step = (IStep) obj;
        } else {
            throw new ClassCastException("Class " + clzName + " is not of type IStep");
        }
        return step;
    }
}
