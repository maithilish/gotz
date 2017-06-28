package org.codetab.gotz;

import javax.inject.Inject;

import org.codetab.gotz.di.DInjector;

public final class Gotz {

    private GotzEngine gotzEngine;

    @Inject
    public Gotz(final GotzEngine gotzEngine) {
        this.gotzEngine = gotzEngine;
    }

    public void start() {
        gotzEngine.start();
    }

    public static void main(final String[] args) {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        Gotz gotz = dInjector.instance(Gotz.class);
        gotz.start();
    }

}
