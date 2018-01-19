package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.zip.DataFormatException;

import javax.script.ScriptException;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.DataHelper;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.persistence.DataPersistence;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.extract.JSoupHtmlParser;
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
    private JSoupHtmlParser parser;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private Labels labels;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        labels = new Labels("x", "y");
        parser.setLabels(labels);
    }

    @Test
    public void testInitialize() throws FieldsNotFoundException {
        String dataDefName = "d";
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

        String dataDefName = "d";
        long dataDefId = 1;
        DataDef dataDef = new DataDef();
        dataDef.setName(dataDefName);
        dataDef.setId(dataDefId);

        long documentId = 2;
        Document document = new Document();
        document.setId(documentId);
        parser.setInput(document);

        Data data = new Data();

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName)).willReturn(dataDef);
        given(dataPersistence.loadData(dataDefId, documentId)).willReturn(data);

        boolean actual = parser.load();

        assertThat(actual).isTrue();

        assertThat(parser.getData()).isSameAs(data);
    }

    @Test
    public void testLoadNoDocumentId()
            throws DataDefNotFoundException, IllegalAccessException {

        String dataDefName = "d";
        long dataDefId = 1;
        DataDef dataDef = new DataDef();
        dataDef.setName(dataDefName);
        dataDef.setId(dataDefId);

        // document id null
        Document document = new Document();
        parser.setInput(document);

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName)).willReturn(dataDef);

        boolean actual = parser.load();

        assertThat(actual).isTrue();

        verifyZeroInteractions(dataPersistence);
    }

    @Test
    public void testLoadNoDataDefId()
            throws DataDefNotFoundException, IllegalAccessException {

        // datadef id null
        String dataDefName = "d";
        DataDef dataDef = new DataDef();
        dataDef.setName(dataDefName);

        long documentId = 2;
        Document document = new Document();
        document.setId(documentId);
        parser.setInput(document);

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName)).willReturn(dataDef);

        boolean actual = parser.load();

        assertThat(actual).isTrue();

        verifyZeroInteractions(dataPersistence);
    }

    @Test
    public void testLoadThrowsException()
            throws DataDefNotFoundException, IllegalAccessException {

        String dataDefName = "d";
        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataDefService.getDataDef(dataDefName))
                .willThrow(DataDefNotFoundException.class);

        testRule.expect(StepRunException.class);
        parser.load();
    }

    @Test
    public void testProcess() throws NumberFormatException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            DataDefNotFoundException, ScriptException, IOException,
            DataFormatException, FieldsException, FieldsNotFoundException {
        String dataDefName = "d";
        long dataDefId = 1;
        DataDef dataDef = new DataDef();
        dataDef.setName(dataDefName);
        dataDef.setId(dataDefId);

        long documentId = 2;
        Document document = new Document();
        document.setId(documentId);
        parser.setInput(document);

        Range<Integer> indexRange = Range.between(1, 1);

        Data data = createTestData();
        Member newMember = createTestData().getMembers().get(0);

        FieldUtils.writeField(parser, "dataDefName", dataDefName, true);
        given(dataHelper.getDataTemplate(dataDefName, documentId,
                parser.getLabel())).willReturn(data);
        given(fieldsHelper.getRange(eq("//xf:indexRange/@value"),
                any(Fields.class))).willReturn(indexRange);
        given(dataHelper.createMember(any(Member.class)))
                .willReturn(newMember);

        parser.process();
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

    @Test
    public void testStore() {

    }

    @Test
    public void testHandover() {

    }

    @Test
    public void testSetValue() {

    }

    @Test
    public void testIsConsistent() {

    }

    @Test
    public void testQueryByQuery() {

    }

    @Test
    public void testQueryByScript() {

    }

    @Test
    public void testGetValue() {

    }

    @Test
    public void testParse() {

    }

    @Test
    public void testSetInput() {

    }

    @Test
    public void testGetDocument() {

    }

    @Test
    public void testIsDocumentLoaded() {

    }

    @Test
    public void testGetData() {

    }

    @Test
    public void testGetStartIndex() {

    }

    @Test
    public void testGetEndIndex() {

    }

    @Test
    public void testGetDataDefName() {

    }

    @Test
    public void testGetBlockBegin() {

    }

    @Test
    public void testGetBlockEnd() {

    }

    @Test
    public void testSetTraceString() {

    }

}

/**
 * to test post initialization
 * @author Maithilish
 *
 */
abstract class TestParser extends BaseParser {

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
        return null;
    }

}
