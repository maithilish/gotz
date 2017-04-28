package org.codetab.gotz.di;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

@Singleton
public class DInjector {

    private Injector injector;

    public DInjector() {
    }

    @Inject
    public DInjector(Injector injector) {
        this.injector = injector;
    }

    public <T> T instance(Class<T> clz) {
        return injector.getInstance(clz);
    }
}
