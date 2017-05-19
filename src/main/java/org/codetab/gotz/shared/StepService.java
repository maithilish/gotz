package org.codetab.gotz.shared;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.step.IStepO;

@Singleton
public class StepService {

    @Inject
    private DInjector dInjector;

    @Inject
    private StepService() {
    }

    public IStepO getStep(final String clzName) throws ClassNotFoundException,
    InstantiationException, IllegalAccessException {
        IStepO step = null;
        Class<?> stepClass = Class.forName(clzName);
        Object obj = dInjector.instance(stepClass);
        if (obj instanceof IStepO) {
            step = (IStepO) obj;
        } else {
            throw new ClassCastException("Class " + clzName + " is not of type IStepO");
        }
        return step;
    }
}
