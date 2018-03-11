package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.CriticalException;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.BreakAfterHelper;
import org.codetab.gotz.model.helper.DataDefHelper;
import org.codetab.gotz.model.helper.DataHelper;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.persistence.DataPersistence;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BaseParserTest {

    @Mock
    private DataPersistence dataPersistence;
    @Mock
    private DataDefService dataDefService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private DataHelper dataHelper;
    @Mock
    private BreakAfterHelper breakAfterHelper;
    @Mock
    private DataDefHelper dataDefHelper;
    @Mock
    private StepService stepService;
    @Mock
    private DInjector dInjector;

    @Mock
    private ActivityService activityService;
    @Mock
    private ConfigService configService;
    @Mock
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private TestParser parser;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private Labels labels;
    private Document document;
    private DataDef dataDef;
    private String dataDefName;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        labels = new Labels("x", "y");
        parser.setLabels(labels);

        dataDefName = "d";
        long dataDefId = 1;
        dataDef = new DataDef();
        dataDef.setName(dataDefName);
        dataDef.setId(dataDefId);

        long documentId = 2;
        document = new Document();
        document.setId(documentId);
        parser.setInput(document);

    }

    @Test
    public void testInitialize() throws FieldsNotFoundException {

        String taskName = "t";

        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@dataDef",
                parser.getFields())).willReturn(dataDefName);
        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@name",
                parser.getFields())).willReturn(taskName);

        boolean actual = parser.initialize();

        assertThat(actual).isTrue();
        assertThat(parser.getDataDefName()).isEqualTo("d");

        Labels actualLabels = parser.getLabels();
        assertThat(actualLabels.getName()).isEqualTo("x");
        assertThat(actualLabels.getGroup()).isEqualTo("y");
        assertThat(actualLabels.getDataDef()).isEqualTo(dataDefName);
        assertThat(actualLabels.getTask()).isEqualTo(taskName);
    }

    @Test
    public void testInitializeThrowsException() throws FieldsNotFoundException {

        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@dataDef",
                parser.getFields())).willThrow(FieldsNotFoundException.class);

        testRule.expect(StepRunException.class);
        parser.initialize();
    }

    @Test
    public void testPostInitialize()
            throws FieldsNotFoundException, IllegalAccessException {

        // postInit returns true
        TestParser testParser = new TestParser() {
            @Override
            protected boolean postInitialize() {
                return true;
            }
        };

        testParser.setLabels(labels);
        FieldUtils.writeField(testParser, "fieldsHelper", fieldsHelper, true);

        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@dataDef",
                testParser.getFields())).willReturn("a", "b");
        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@name",
                testParser.getFields())).willReturn("x", "y");

        boolean actual = testParser.initialize();

        assertThat(actual).isTrue();

        // postInit returns false
        testParser = new TestParser() {
            @Override
            protected boolean postInitialize() {
                return false;
            }
        };

        testParser.setLabels(labels);
        FieldUtils.writeField(testParser, "fieldsHelper", fieldsHelper, true);

        actual = testParser.initialize();

        assertThat(actual).isFalse();
    }

    @Test
    public void testLoad()
            throws DataDefNotFoundException, IllegalAccessException {

        Data data = new Data();

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName)).willReturn(dataDef);
        given(dataPersistence.loadData(dataDef.getId(), document.getId()))
                .willReturn(data);

        boolean actual = parser.load();

        assertThat(actual).isTrue();

        assertThat(parser.getData()).isSameAs(data);
    }

    @Test
    public void testLoadNoDocumentId()
            throws DataDefNotFoundException, IllegalAccessException {

        document.setId(null);

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName)).willReturn(dataDef);

        boolean actual = parser.load();

        assertThat(actual).isTrue();

        verifyZeroInteractions(dataPersistence);
    }

    @Test
    public void testLoadNoDataDefId()
            throws DataDefNotFoundException, IllegalAccessException {

        dataDef.setId(null);

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName)).willReturn(dataDef);

        boolean actual = parser.load();

        assertThat(actual).isTrue();

        verifyZeroInteractions(dataPersistence);
    }

    @Test
    public void testLoadThrowsException()
            throws DataDefNotFoundException, IllegalAccessException {

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName))
                .willThrow(DataDefNotFoundException.class);

        testRule.expect(StepRunException.class);
        parser.load();
    }

    @Test
    public void testProcessParseNoPushNewMember() throws NumberFormatException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            DataDefNotFoundException, ScriptException, IOException,
            DataFormatException, FieldsException, FieldsNotFoundException {

        Range<Integer> indexRange = Range.between(1, 1);

        Data data = createTestData();
        List<Member> members = data.getMembers();

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataHelper.getDataTemplate(dataDefName, document.getId(),
                parser.getLabel())).willReturn(data);
        given(fieldsHelper.getRange(eq("//xf:indexRange/@value"),
                any(Fields.class))).willReturn(indexRange);
        given(breakAfterHelper.hasFinished(any(Axis.class), anyInt()))
                .willReturn(true);

        parser.process();

        assertThat(data.getMembers().size()).isEqualTo(1);
        assertThat(data.getMembers()).isEqualTo(members);
        assertThat(data.getMembers()).isNotSameAs(members);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessParsePushNewMember() throws NumberFormatException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            DataDefNotFoundException, ScriptException, IOException,
            DataFormatException, FieldsException, FieldsNotFoundException {

        Range<Integer> indexRange = Range.between(1, 1);

        Data data = createTestData();
        List<Member> members = data.getMembers();
        Member member = data.getMembers().get(0);
        Member newMember = createTestData().getMembers().get(0);
        Integer[] indexes = new Integer[4];
        indexes[AxisName.COL.ordinal()] = 2;
        indexes[AxisName.ROW.ordinal()] = 2;
        indexes[AxisName.FACT.ordinal()] = 2;

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataHelper.getDataTemplate(dataDefName, document.getId(),
                parser.getLabel())).willReturn(data);
        given(fieldsHelper.getRange(eq("//xf:indexRange/@value"),
                any(Fields.class))).willReturn(indexRange);
        given(dataHelper.createMember(member)).willReturn(newMember);
        given(dataHelper.nextMemberIndexes(member, AxisName.COL))
                .willReturn(indexes).willReturn(indexes);
        given(dataHelper.nextMemberIndexes(member, AxisName.ROW))
                .willReturn(indexes).willReturn(indexes);
        doReturn(false).doReturn(true).when(dataHelper)
                .alreadyProcessed(any(Set.class), any(Integer[].class));

        given(breakAfterHelper.hasFinished(member.getAxis(AxisName.ROW), 1))
                .willReturn(false).willReturn(true);

        given(breakAfterHelper.hasFinished(newMember.getAxis(AxisName.COL), 1))
                .willReturn(true);

        given(breakAfterHelper.hasFinished(member.getAxis(AxisName.COL), 1))
                .willReturn(false);

        parser.process();

        assertThat(data.getMembers().size()).isEqualTo(2);
        assertThat(data.getMembers().get(0)).isSameAs(member);
        assertThat(data.getMembers().get(1)).isSameAs(newMember);
        assertThat(data.getMembers()).isNotSameAs(members);

        assertThat(newMember.getAxis(AxisName.COL).getIndex())
                .isEqualTo(member.getAxis(AxisName.COL).getIndex() + 1);
        assertThat(newMember.getAxis(AxisName.ROW).getIndex())
                .isEqualTo(member.getAxis(AxisName.ROW).getIndex());
        assertThat(newMember.getAxis(AxisName.FACT).getIndex())
                .isEqualTo(member.getAxis(AxisName.FACT).getIndex());

        assertThat(newMember.getAxis(AxisName.COL).getOrder())
                .isEqualTo(member.getAxis(AxisName.COL).getOrder() + 1);
        assertThat(newMember.getAxis(AxisName.ROW).getOrder())
                .isEqualTo(member.getAxis(AxisName.ROW).getOrder());
        assertThat(newMember.getAxis(AxisName.FACT).getOrder())
                .isEqualTo(member.getAxis(AxisName.FACT).getOrder());
        assertThat(newMember.getAxis(AxisName.COL).getValue()).isNull();
        assertThat(newMember.getAxis(AxisName.ROW).getValue()).isNull();
        assertThat(newMember.getAxis(AxisName.FACT).getValue()).isNull();
    }

    @Test
    public void testProcessParsePushNewMemberIndexRangeNotFound()
            throws NumberFormatException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, DataDefNotFoundException, ScriptException,
            IOException, DataFormatException, FieldsException,
            FieldsNotFoundException {

        Data data = createTestData();
        List<Member> members = data.getMembers();

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataHelper.getDataTemplate(dataDefName, document.getId(),
                parser.getLabel())).willReturn(data);
        given(fieldsHelper.getRange(eq("//xf:indexRange/@value"),
                any(Fields.class))).willThrow(FieldsNotFoundException.class);
        given(breakAfterHelper.hasFinished(any(Axis.class), eq(-1)))
                .willReturn(true);

        parser.process();

        assertThat(data.getMembers().size()).isEqualTo(1);
        assertThat(data.getMembers()).isEqualTo(members);
        assertThat(data.getMembers()).isNotSameAs(members);

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testProcessThrowsException()
            throws NumberFormatException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, DataDefNotFoundException, ScriptException,
            IOException, DataFormatException, FieldsException {

        Class[] clzs = {NumberFormatException.class,
                ClassNotFoundException.class, IllegalAccessException.class,
                InvocationTargetException.class, NoSuchMethodException.class,
                DataDefNotFoundException.class, ScriptException.class,
                IOException.class, DataFormatException.class,
                FieldsException.class};

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);

        given(dataHelper.getDataTemplate(any(String.class),
                eq(document.getId()), any(String.class))).willThrow(clzs[0])
                        .willThrow(clzs[1]).willThrow(clzs[2])
                        .willThrow(clzs[3]).willThrow(clzs[4])
                        .willThrow(clzs[5]).willThrow(clzs[6])
                        .willThrow(clzs[7]).willThrow(clzs[8])
                        .willThrow(clzs[9]);

        for (Class clz : clzs) {
            try {
                parser.process();
                fail("should throw exception");
            } catch (StepRunException e) {
                assertThat(e.getCause()).isInstanceOf(clz);
            }
        }
    }

    @Test
    public void testProcessDataNotNull() throws NumberFormatException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            DataDefNotFoundException, ScriptException, IOException,
            DataFormatException, FieldsException, FieldsNotFoundException {
        FieldUtils.writeField(parser, "data", new Data(), true);
        boolean actual = parser.process();
        assertThat(actual).isTrue();
    }

    @Test
    public void testStore() throws IllegalAccessException {
        Fields fields = TestUtil.createEmptyFields();

        Data data = new Data();
        data.setId(1L);
        Data loadedData = new Data();
        data.setId(2L);

        parser.setFields(fields);
        FieldUtils.writeField(parser, "data", data, true);

        given(dataPersistence.storeData(data, fields)).willReturn(true);
        given(dataPersistence.loadData(data.getId())).willReturn(loadedData);

        boolean actual = parser.store();

        assertThat(actual).isTrue();
        assertThat(parser.getData()).isSameAs(loadedData);
    }

    @Test
    public void testNotStored() throws IllegalAccessException {

        Fields fields = TestUtil.createEmptyFields();

        Data data = new Data();
        data.setId(1L);
        Data loadedData = new Data();
        data.setId(2L);

        parser.setFields(fields);
        FieldUtils.writeField(parser, "data", data, true);

        given(dataPersistence.storeData(data, fields)).willReturn(false);
        given(dataPersistence.loadData(data.getId())).willReturn(loadedData);

        boolean actual = parser.store();

        assertThat(actual).isTrue();
        assertThat(parser.getData()).isSameAs(data);
    }

    @Test
    public void testHandover() throws IllegalAccessException {

        Fields fields =
                new XOBuilder<Fields>().add("<xf:x>x</xf:x>").buildFields();
        Data data = new Data();
        data.setId(1L);

        parser.setFields(fields);
        FieldUtils.writeField(parser, "data", data, true);

        boolean actual = parser.handover();

        assertThat(actual).isTrue();
        verify(stepService).pushTask(parser, data, labels, fields);
    }

    @Test
    public void testHandoverThrowsException() throws IllegalAccessException {
        Fields fields = new Fields();
        parser.setFields(fields);

        testRule.expect(StepRunException.class);
        parser.handover();
    }

    @Test
    public void testQueryByScript() throws ScriptException {

        parser.setInput(document);

        ScriptEngine scriptEngine =
                new ScriptEngineManager().getEngineByName("JavaScript");
        given(dataHelper.getScriptEngine()).willReturn(scriptEngine);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("script", "document.getId()");
        String actual = parser.queryByScript(scripts);

        assertThat(actual).isEqualTo(ConvertUtils.convert(document.getId()));
        assertThat(scriptEngine.get("configs")).isSameAs(configService);
        assertThat(scriptEngine.get("document")).isSameAs(document);
    }

    @Test
    public void testQueryByScriptScriptEngineNotNull()
            throws ScriptException, IllegalAccessException {

        ScriptEngine scriptEngine =
                new ScriptEngineManager().getEngineByName("JavaScript");
        FieldUtils.writeField(parser, "jsEngine", scriptEngine, true);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("script", "document.getId()");
        String actual = parser.queryByScript(scripts);

        assertThat(actual).isEqualTo(ConvertUtils.convert(document.getId()));
        assertThat(scriptEngine.get("configs")).isSameAs(configService);
        assertThat(scriptEngine.get("document")).isSameAs(document);
    }

    @Test
    public void testQueryByScriptScriptEngineIsNull()
            throws ScriptException, IllegalAccessException {

        given(dataHelper.getScriptEngine()).willReturn(null);

        Map<String, String> scripts = new HashMap<>();

        testRule.expect(CriticalException.class);
        parser.queryByScript(scripts);

    }

    @Test
    public void testGetValueQueryNullFields()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ScriptException, FieldsNotFoundException {

        Fields fields = null;

        DAxis dAxis = new DAxis();
        dAxis.setName("col");
        dAxis.setFields(fields);

        Data data = createTestData();
        Member member = data.getMembers().get(0);
        Axis axis = member.getAxis(AxisName.COL);

        given(dataDefHelper.getAxis(dataDef, axis.getName())).willReturn(dAxis);

        testRule.expect(StepRunException.class);
        parser.getValue("Test page", dataDef, member, axis);
    }

    @Test
    public void testGetValueQueryNoScriptQueryAndPrefix()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ScriptException, FieldsNotFoundException {

        Fields fields = TestUtil.createEmptyFields();

        DAxis dAxis = new DAxis();
        dAxis.setName("col");
        dAxis.setFields(fields);

        Data data = createTestData();
        Member member = data.getMembers().get(0);
        Axis axis = member.getAxis(AxisName.COL);

        given(dataDefHelper.getAxis(dataDef, axis.getName())).willReturn(dAxis);
        given(fieldsHelper.getLastValue("/xf:script/@script", fields))
                .willThrow(FieldsNotFoundException.class);
        given(fieldsHelper.getLastValue("/xf:query/@region", fields))
                .willThrow(FieldsNotFoundException.class);
        given(fieldsHelper.getValues("/xf:prefix", false, fields))
                .willThrow(FieldsNotFoundException.class);

        String actual = parser.getValue("Test page", dataDef, member, axis);

        assertThat(actual).isNull();
    }

    @Test
    public void testGetValueQueryByScript()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ScriptException, FieldsNotFoundException {

        Fields fields = TestUtil.createEmptyFields();

        DAxis dAxis = new DAxis();
        dAxis.setName("col");
        dAxis.setFields(fields);

        ScriptEngine scriptEngine =
                new ScriptEngineManager().getEngineByName("JavaScript");

        Data data = createTestData();
        Member member = data.getMembers().get(0);
        Axis axis = member.getAxis(AxisName.COL);

        given(dataDefHelper.getAxis(dataDef, axis.getName())).willReturn(dAxis);
        given(dataHelper.getScriptEngine()).willReturn(scriptEngine);
        given(fieldsHelper.getLastValue("/xf:script/@script", fields))
                .willReturn("document.getId()");

        given(fieldsHelper.getLastValue("/xf:query/@region", fields))
                .willThrow(FieldsNotFoundException.class);
        given(fieldsHelper.getValues("/xf:prefix", false, fields))
                .willThrow(FieldsNotFoundException.class);

        String actual = parser.getValue("Test page", dataDef, member, axis);

        assertThat(actual).isEqualTo("2");
    }

    @Test
    public void testGetValueQueryByQuery()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ScriptException, FieldsNotFoundException {

        Fields fields = TestUtil.createEmptyFields();

        DAxis dAxis = new DAxis();
        dAxis.setName("col");
        dAxis.setFields(fields);

        Data data = createTestData();
        Member member = data.getMembers().get(0);
        Axis axis = member.getAxis(AxisName.COL);

        Map<String, String> queries = new HashMap<>();
        queries.put("region", "qr");
        queries.put("field", "qf");
        queries.put("attribute", "qa");

        given(dataDefHelper.getAxis(dataDef, axis.getName())).willReturn(dAxis);

        given(fieldsHelper.getLastValue("/xf:query/@region", fields))
                .willReturn(queries.get("region"));
        given(fieldsHelper.getLastValue("/xf:query/@field", fields))
                .willReturn(queries.get("field"));
        given(fieldsHelper.getLastValue("/xf:query/@attribute", fields))
                .willReturn(queries.get("attribute"));

        given(fieldsHelper.getLastValue("/xf:script/@script", fields))
                .willThrow(FieldsNotFoundException.class);
        given(fieldsHelper.getValues("/xf:prefix", false, fields))
                .willThrow(FieldsNotFoundException.class);

        String actual = parser.getValue("Test page", dataDef, member, axis);

        assertThat(actual).isEqualTo("test value");
        verify(fieldsHelper).replaceVariables(queries, member.getAxisMap());

        // no attribute test
        queries.put("attribute", "");
        given(fieldsHelper.getLastValue("/xf:query/@attribute", fields))
                .willThrow(FieldsNotFoundException.class);

        actual = parser.getValue("Test page", dataDef, member, axis);

        assertThat(actual).isEqualTo("test value");
        verify(fieldsHelper).replaceVariables(queries, member.getAxisMap());
    }

    @Test
    public void testGetValuePrefix()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ScriptException, FieldsNotFoundException {

        Fields fields = TestUtil.createEmptyFields();

        DAxis dAxis = new DAxis();
        dAxis.setName("col");
        dAxis.setFields(fields);

        ScriptEngine scriptEngine =
                new ScriptEngineManager().getEngineByName("JavaScript");

        Data data = createTestData();
        Member member = data.getMembers().get(0);
        Axis axis = member.getAxis(AxisName.COL);

        List<String> prefixes = Arrays.asList("x", "y");

        given(dataDefHelper.getAxis(dataDef, axis.getName())).willReturn(dAxis);
        given(dataHelper.getScriptEngine()).willReturn(scriptEngine);
        given(fieldsHelper.getLastValue("/xf:script/@script", fields))
                .willReturn("document.getId()");

        given(fieldsHelper.getLastValue("/xf:query/@region", fields))
                .willThrow(FieldsNotFoundException.class);
        given(fieldsHelper.getValues("/xf:prefix", false, fields))
                .willReturn(prefixes);
        given(fieldsHelper.prefixValue("2", prefixes)).willReturn("xyz");

        String actual = parser.getValue("Test page", dataDef, member, axis);

        assertThat(actual).isEqualTo("xyz");
    }

    @Test
    public void testGetValuePrefixThrowsException()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ScriptException, FieldsNotFoundException {

        Fields fields = TestUtil.createEmptyFields();

        DAxis dAxis = new DAxis();
        dAxis.setName("col");
        dAxis.setFields(fields);

        List<String> prefixes = Arrays.asList("x", "y");

        ScriptEngine scriptEngine =
                new ScriptEngineManager().getEngineByName("JavaScript");

        Data data = createTestData();
        Member member = data.getMembers().get(0);
        Axis axis = member.getAxis(AxisName.COL);

        given(dataDefHelper.getAxis(dataDef, axis.getName())).willReturn(dAxis);
        given(dataHelper.getScriptEngine()).willReturn(scriptEngine);
        given(fieldsHelper.getLastValue("/xf:script/@script", fields))
                .willThrow(FieldsNotFoundException.class);

        given(fieldsHelper.getLastValue("/xf:query/@region", fields))
                .willThrow(FieldsNotFoundException.class);
        given(fieldsHelper.getValues("/xf:prefix", false, fields))
                .willReturn(prefixes);

        testRule.expect(StepRunException.class);
        parser.getValue("Test page", dataDef, member, axis);
    }

    @Test
    public void testIsConsistent() throws IllegalAccessException {
        boolean actual = parser.isConsistent();
        assertThat(actual).isFalse();

        parser.setConsistent(true);

        FieldUtils.writeField(parser, "data", null, true);
        actual = parser.isConsistent();
        assertThat(actual).isFalse();

        FieldUtils.writeField(parser, "data", new Data(), true);

        actual = parser.isConsistent();
        assertThat(actual).isTrue();
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        parser.setInput(document);
        Document actual = parser.getDocument();
        assertThat(actual).isSameAs(document);
    }

    @Test
    public void testSetInputShouldThrowException()
            throws IllegalAccessException {
        FieldUtils.writeField(parser, "document", null, true);
        testRule.expect(StepRunException.class);
        parser.setInput("xyz");
    }

    @Test
    public void testIsDocumentLoaded() {
        TestParser testParser = new TestParser();
        testParser.setInput(document);
        assertThat(testParser.isDocumentLoaded()).isFalse();

        document.setDocumentObject("xyz");

        assertThat(testParser.isDocumentLoaded()).isTrue();
    }

    @Test
    public void testGetData() throws IllegalAccessException {
        Data data = new Data();
        FieldUtils.writeField(parser, "data", data, true);

        assertThat(parser.getData()).isSameAs(data);
    }

    @Test
    public void testGetStartIndex()
            throws NumberFormatException, FieldsNotFoundException {
        Fields fields = TestUtil.createEmptyFields();

        Range<Integer> range = Range.between(5, 7);

        given(fieldsHelper.getRange("//xf:indexRange/@value", fields))
                .willReturn(range);

        Integer actual = parser.getStartIndex(fields);

        assertThat(actual).isEqualTo(range.getMinimum());
    }

    @Test
    public void testGetEndIndex()
            throws NumberFormatException, FieldsNotFoundException {
        Fields fields = TestUtil.createEmptyFields();

        Range<Integer> range = Range.between(5, 7);

        given(fieldsHelper.getRange("//xf:indexRange/@value", fields))
                .willReturn(range);

        Integer actual = parser.getEndIndex(fields);

        assertThat(actual).isEqualTo(range.getMaximum());

    }

    @Test
    public void testGetDataDefName() throws IllegalAccessException {
        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        assertThat(parser.getDataDefName()).isSameAs(dataDefName);
    }

    @Test
    public void testGetBlockBegin() throws FieldsNotFoundException {
        String expected =
                System.lineSeparator() + "---------" + System.lineSeparator();

        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@dataDef",
                parser.getFields())).willReturn(dataDefName);
        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@name",
                parser.getFields())).willReturn("t");

        parser.initialize();
        assertThat(parser.getBlockBegin()).isEqualTo(expected);
    }

    @Test
    public void testGetBlockEnd() throws FieldsNotFoundException {
        String expected = System.lineSeparator() + "---------" + "---------";

        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@dataDef",
                parser.getFields())).willReturn(dataDefName);
        given(fieldsHelper.getLastValue("/xf:fields/xf:task/@name",
                parser.getFields())).willReturn("t");

        parser.initialize();
        assertThat(parser.getBlockEnd()).isEqualTo(expected);

    }

    @Test
    public void testSetTraceString() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");

        String header = "header";
        String expected =
                String.join(System.lineSeparator(), "", header, "k : v", "");
        parser.setTraceString(sb, map, header, true);
        assertThat(sb.toString()).isEqualTo(expected);

        expected = String.join(System.lineSeparator(), "", "k : v", "");
        sb = new StringBuilder();
        parser.setTraceString(sb, map, null, true);
        assertThat(sb.toString()).isEqualTo(expected);

        expected = "";
        sb = new StringBuilder();
        parser.setTraceString(sb, map, header, false);
        assertThat(sb.toString()).isEqualTo(expected);
    }

    private Data createTestData() {
        Fields fields = new XOBuilder<Fields>()
                .add("<xf:indexRange value='1-1' />").buildFields();
        Axis col = new Axis();
        col.setName(AxisName.COL);
        col.setIndex(1);
        col.setOrder(1);
        col.setFields(fields);
        Axis row = new Axis();
        row.setName(AxisName.ROW);
        row.setIndex(1);
        row.setOrder(1);
        row.setFields(fields);
        Axis fact = new Axis();
        fact.setName(AxisName.FACT);
        fact.setIndex(1);
        fact.setOrder(1);
        fact.setFields(fields);

        Member member = new Member();
        member.addAxis(col);
        member.addAxis(row);
        member.addAxis(fact);

        Data data = new Data();
        data.addMember(member);

        return data;
    }

}

/**
 * to test post initialization
 * @author Maithilish
 *
 */
class TestParser extends BaseParser {

    @Override
    public IStep instance() {
        return null;
    }

    @Override
    protected void setValue(final DataDef dataDef, final Member member)
            throws ScriptException, NumberFormatException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, MalformedURLException, IOException,
            DataFormatException {
    }

    @Override
    protected String queryByQuery(final Object page,
            final Map<String, String> queries) {
        return "test value";
    }

    @Override
    protected boolean postInitialize() {
        return true;
    }

}
