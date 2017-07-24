package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.appender.ListAppender;
import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.shared.ConfigService;
import org.junit.AfterClass;
import org.junit.Test;

public class ItcPriceIT {

    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.gotz.model.DataDef");
        schemaClasses.add("org.codetab.gotz.model.Locator");
        schemaClasses.add("org.codetab.gotz.model.Document");
        schemaClasses.add("org.codetab.gotz.model.Data");

        daoUtil = DaoUtilFactory.getDaoFactory(ORM.JDO).getUtilDao();
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
    }

    @Test
    public void itcPriceJSoupTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/price/bean.xml");

        DInjector dInjector = new DInjector().instance(DInjector.class);
        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        gotzEngine.start();

        ConfigService configService = dInjector.instance(ConfigService.class);
        AppenderService appenderService =
                dInjector.instance(AppenderService.class);
        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<Object> actual = listAppender.getList();
        List<String> expected = Arrays.asList("ITC |quote |"
                + configService.getRunDateTime() + " |Price |315.25");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void itcPriceHtmlUnitTest() {

        System.setProperty("gotz.beanFile",
                "/itest/itc/price/htmlunit/bean.xml");

        DInjector dInjector = new DInjector().instance(DInjector.class);
        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        gotzEngine.start();

        ConfigService configService = dInjector.instance(ConfigService.class);
        AppenderService appenderService =
                dInjector.instance(AppenderService.class);
        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<Object> actual = listAppender.getList();
        List<String> expected = Arrays.asList("ITC |quote |"
                + configService.getRunDateTime() + " |Price |315.25");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
