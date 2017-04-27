package org.codetab.gotz;

import org.codetab.gotz.di.BasicModule;
import org.codetab.gotz.di.DInjector;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Gotz {

    private GotzEngine gotzEngine;

    public Gotz(GotzEngine gotzEngine) {
        this.gotzEngine = gotzEngine;
    }

    public void start(){
        gotzEngine.start();
    }

    public static void main(final String[] args) {
        Injector injector = Guice.createInjector(new BasicModule());
        DInjector dInjector = injector.getInstance(DInjector.class);
        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);

        Gotz gotz = new Gotz(gotzEngine);
        gotz.start();
    }

}
