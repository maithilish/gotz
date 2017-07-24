package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
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
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.AppenderService;
import org.junit.AfterClass;
import org.junit.Test;

public class ItcLocatorIT {

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
    public void itcLocatorJSoupTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/locator/bean.xml");

        DInjector dInjector = new DInjector().instance(DInjector.class);
        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        gotzEngine.start();

        AppenderService appenderService =
                dInjector.instance(AppenderService.class);
        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<String> actual = new ArrayList<>();
        List<Object> list = listAppender.getList();
        for (Object obj : list) {
            Locator locator = (Locator) obj;
            actual.add(locator.getUrl());
        }

        List<String> expected = Arrays.asList(
                "http://www.moneycontrol.com/financials/itc/balance-sheet/ITC#ITC",
                "http://www.moneycontrol.com/financials/itc/profit-loss/ITC#ITC");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void itcLocatorHtmlUnitTest() {

        System.setProperty("gotz.beanFile",
                "/itest/itc/locator/htmlunit/bean.xml");

        DInjector dInjector = new DInjector().instance(DInjector.class);
        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        gotzEngine.start();

        AppenderService appenderService =
                dInjector.instance(AppenderService.class);
        Appender ap = appenderService.getAppender("list");
        ListAppender listAppender = null;
        if (ap instanceof ListAppender) {
            listAppender = (ListAppender) ap;
        }

        List<String> actual = new ArrayList<>();
        List<Object> list = listAppender.getList();
        for (Object obj : list) {
            Locator locator = (Locator) obj;
            actual.add(locator.getUrl());
        }

        List<String> expected = Arrays.asList(
                "http://www.moneycontrol.com/financials/itc/balance-sheet/ITC#ITC",
                "http://www.moneycontrol.com/financials/itc/profit-loss/ITC#ITC");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
