package org.codetab.gotz.di;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.codetab.gotz.Gotz;
import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.dao.DaoFactoryProvider;
import org.codetab.gotz.dao.jdo.PMF;
import org.codetab.gotz.helper.IXoc;
import org.codetab.gotz.helper.JaxbXoc;
import org.codetab.gotz.model.Activity;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.util.ResourceStream;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

public class BasicModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Gotz.class);
        bind(GotzEngine.class);
        bind(ConfigService.class);
        bind(BeanService.class);
        bind(DataDefService.class);
        bind(DInjector.class);
        bind(DaoFactoryProvider.class);
        bind(PMF.class);
        bind(Timer.class);
        bind(ActivityService.class);
        bind(ResourceStream.class);

        bind(IXoc.class).to(JaxbXoc.class);

        bind(new TypeLiteral<List<Activity>>() {
        }).toInstance(new ArrayList<Activity>());
    }

    @Provides
    Runtime provideRuntime() {
        return Runtime.getRuntime();
    }

}
