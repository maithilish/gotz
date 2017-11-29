package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.dao.jdo.LocatorDao;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.step.load.appender.ListAppender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ItcQuotePageIT {

    private static IDaoUtil daoUtil;
    private static PersistenceManagerFactory pmf;
    private static HashSet<String> schemaClasses;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.gotz.model.DataDef");
        schemaClasses.add("org.codetab.gotz.model.Locator");
        schemaClasses.add("org.codetab.gotz.model.Document");
        schemaClasses.add("org.codetab.gotz.model.Data");

        daoUtil = DaoUtilFactory.getDaoFactory(ORM.JDO).getUtilDao();
        pmf = daoUtil.getPersistenceManagerFactory();

        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
    }

    @Before
    public void setUp() {
        // create schema
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @After
    public void tearDown() {
        // drop schema
        daoUtil.deleteSchemaForClasses(schemaClasses);
        // clear cache
        daoUtil.clearCache();
    }

    @Test
    public void itcQuotePageJSoupTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/quotepage/bean.xml");

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

        List<Object> actual = listAppender.getList();
        LocatorDao locatorDao = new LocatorDao(pmf);
        Locator locator = locatorDao.getLocator("ITC", "quote");
        Date fromDate = locator.getDocuments().get(0).getFromDate();

        String prefix = "ITC|quote|" + fromDate + "|";
        List<String> expected = getJSoupExpected(prefix);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void itcQuotePageHtmlUnitTest() {

        System.setProperty("gotz.beanFile",
                "/itest/itc/quotepage/htmlunit/bean.xml");

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

        List<Object> actual = listAppender.getList();

        LocatorDao locatorDao = new LocatorDao(pmf);
        Locator locator = locatorDao.getLocator("ITC", "quote");
        Date fromDate = locator.getDocuments().get(0).getFromDate();

        String prefix = "ITC|quote|" + fromDate + "|";
        List<String> expected = getHtmlUnitExpected(prefix);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    private List<String> getHtmlUnitExpected(final String prefix) {
        List<String> expected = new ArrayList<>();
        expected.add(prefix + "EPS (TTM)|* -");
        addExpected(prefix, expected);
        return expected;
    }

    private List<String> getJSoupExpected(final String prefix) {
        List<String> expected = new ArrayList<>();
        expected.add(prefix + "EPS (TTM)|-");
        addExpected(prefix, expected);
        return expected;
    }

    private void addExpected(final String prefix, final List<String> expected) {
        expected.add(prefix + "DIV YIELD.(%)|2.70%");
        expected.add(prefix + "P/E|-");
        expected.add(prefix + "P/C|-");
        expected.add(prefix + "FACE VALUE (Rs)|1.00");
        expected.add(prefix + "MARKET CAP (Rs Cr)|382,642.57");
        expected.add(prefix + "INDUSTRY P/E|37.06");
        expected.add(prefix + "PRICE/BOOK|11.29");
        expected.add(prefix + "BOOK VALUE (Rs)|27.89");
        expected.add(prefix + "DIV (%)|850.00%");
    }
}
