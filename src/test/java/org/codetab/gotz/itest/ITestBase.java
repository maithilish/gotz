package org.codetab.gotz.itest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StrSubstitutor;
import org.codetab.gotz.GotzEngine;
import org.codetab.gotz.dao.DaoUtilFactory;
import org.codetab.gotz.dao.IDaoUtil;
import org.codetab.gotz.dao.ORM;
import org.codetab.gotz.dao.jdo.LocatorDao;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class ITestBase {

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

    protected List<String> runGotz(final String beanFile,
            final DInjector dInjector)
            throws FileNotFoundException, IOException {
        System.setProperty("gotz.useDatastore", "true");
        System.setProperty("gotz.beanFile", beanFile);

        // we delete the example output file in developer area
        String outputFile = "output/data.txt";
        FileUtils.deleteQuietly(new File(outputFile));

        GotzEngine gotzEngine = dInjector.instance(GotzEngine.class);
        gotzEngine.start();

        return IOUtils.readLines(new FileInputStream(outputFile), "UTF-8");
    }

    protected List<String> getExpectedList(final String expectedFile,
            final DInjector dInjector) {
        List<String> expectedList = TestUtil.readFileAsList(expectedFile);
        expectedList = substituteVariables(expectedList, dInjector);
        return expectedList;
    }

    private List<String> substituteVariables(final List<String> strings,
            final DInjector dInjector) {
        ConfigService cs = dInjector.instance(ConfigService.class);
        Date runDateTime = cs.getRunDateTime();

        Map<String, String> map = new HashMap<>();
        map.put("runDateTime", runDateTime.toString());

        LocatorDao locatorDao = new LocatorDao(pmf);
        Locator locator = locatorDao.getLocator("acme", "quote");
        if (locator != null) {
            Date fromDate = locator.getDocuments().get(0).getFromDate();
            map.put("documentFromDate", fromDate.toString());
        }

        StrSubstitutor ss = new StrSubstitutor(map);
        ss.setVariablePrefix("%{"); //$NON-NLS-1$
        ss.setVariableSuffix("}"); //$NON-NLS-1$
        ss.setEscapeChar('%');

        List<String> list = new ArrayList<>();
        for (String str : strings) {
            list.add(ss.replace(str));
        }
        return list;
    }
}
