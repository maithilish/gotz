package org.codetab.gotz.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.load.appender.Appender;
import org.codetab.gotz.step.load.appender.ListAppender;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ItcPlIT {

    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.gotz.model.DataDef");
        schemaClasses.add("org.codetab.gotz.model.Locator");
        schemaClasses.add("org.codetab.gotz.model.Document");
        schemaClasses.add("org.codetab.gotz.model.Data");

        daoUtil = DaoUtilFactory.getDaoFactory(ORM.JDO).getUtilDao();
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

    /*
     * uses JSoup Parser
     */
    @Test
    public void itcPlJSoupTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/pl/bean.xml");

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
        List<String> expected =
                TestUtil.readFileAsList("/itest/itc/pl/expected.txt");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    /*
     * uses HtmlUnit HtmlParser
     */
    @Test
    public void itcPlHtmlUnitTest() {

        System.setProperty("gotz.beanFile", "/itest/itc/pl/htmlunit/bean.xml");

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
        List<String> expected =
                TestUtil.readFileAsList("/itest/itc/pl/htmlunit/expected.txt");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
