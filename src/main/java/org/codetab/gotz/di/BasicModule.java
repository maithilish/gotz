package org.codetab.gotz.di;

import org.codetab.gotz.dao.DaoFactory;
import org.codetab.gotz.dao.jdo.PMF;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.MonitorService;

import com.google.inject.AbstractModule;


public class BasicModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MonitorService.class);
        bind(ConfigService.class);
        bind(BeanService.class);
        bind(DataDefService.class);
        bind(DInjector.class);
        bind(DaoFactory.class);
        bind(PMF.class);
    }

}
