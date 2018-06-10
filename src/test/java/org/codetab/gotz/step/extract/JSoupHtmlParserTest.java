package org.codetab.gotz.step.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import javax.script.ScriptException;

import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XOBuilder;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JSoupHtmlParserTest {

    private JSoupHtmlParser parser;

    private Labels labels;

    private static DInjector dInjector;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        dInjector = new DInjector();
    }

    @Before
    public void setUp() throws Exception {
        parser = dInjector.instance(JSoupHtmlParser.class);
        labels = new Labels("x", "y");
        parser.setLabels(labels);
    }

    @Test
    public void testInstance() {
        assertThat(parser.isConsistent()).isFalse();
        assertThat(parser.getStepType()).isNull();
        assertThat(parser.instance()).isInstanceOf(JSoupHtmlParser.class);
        assertThat(parser.instance()).isSameAs(parser.instance());
    }

    @Test
    public void testPostInitialize() throws IOException {
        String html = "<div id='test' ><p>x</div></p>";

        DocumentHelper dh = dInjector.instance(DocumentHelper.class);
        Document document = new Document();
        document.setUrl("/home/x");
        dh.setDocumentObject(document, html.getBytes());
        parser.setInput(document);

        parser.postInitialize();

        assertThat(parser.postInitialize()).isTrue();
    }

    @Test
    public void testPostInitializeDocumentNotLoaded() throws IOException {

        // document without documentObject
        Document document = new Document();
        parser.setInput(document);

        try {
            parser.postInitialize();
            fail("should throw exception");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void testSetValue()
            throws NumberFormatException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException,
            DataFormatException, IOException {
        String html = "<div id='test'><p>x</div></p>";

        DocumentHelper dh = dInjector.instance(DocumentHelper.class);
        Document document = new Document();
        dh.setDocumentObject(document, html.getBytes());
        parser.setInput(document);

        DataDef dataDef = createTestDataDef();
        Member member = createTestMember();

        parser.postInitialize();
        parser.setValue(dataDef, member);

        assertThat(member.getAxis(AxisName.COL).getValue()).isEqualTo("x");
    }

    @Test
    public void testSetValueNullIndex()
            throws NumberFormatException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException,
            DataFormatException, IOException {
        String html = "<div id='test'><p>x</div></p>";

        DocumentHelper dh = dInjector.instance(DocumentHelper.class);
        Document document = new Document();
        dh.setDocumentObject(document, html.getBytes());
        parser.setInput(document);

        DataDef dataDef = createTestDataDef();

        Member member = createTestMember();
        member.getAxis(AxisName.COL).setIndex(null);

        parser.postInitialize();
        parser.setValue(dataDef, member);

        assertThat(member.getAxis(AxisName.COL).getValue()).isEqualTo("x");
        assertThat(member.getAxis(AxisName.COL).getIndex()).isEqualTo(2);

        // no indexRange field
        member.getAxis(AxisName.COL).setFields(TestUtil.createEmptyFields());
        member.getAxis(AxisName.COL).setIndex(null);

        parser.setValue(dataDef, member);

        assertThat(member.getAxis(AxisName.COL).getValue()).isEqualTo("x");
        assertThat(member.getAxis(AxisName.COL).getIndex()).isEqualTo(1);

    }

    @Test
    public void testQueryByQueryWithAttribute()
            throws IOException, NumberFormatException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException,
            DataFormatException {

        String html = "<div id='test'><a href='example.org'></a></div>";

        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        Map<String, String> queries = new HashMap<>();
        queries.put("region", "div#test");
        queries.put("field", "a");
        queries.put("attribute", "href");

        String actual = parser.queryByQuery(jsoupDoc, queries);

        assertThat(actual).isEqualTo("example.org");

        actual = parser.queryByQuery(jsoupDoc, queries);

        assertThat(actual).isEqualTo("example.org");
    }

    @Test
    public void testqueryByQueryThrowsException() {

        testRule.expect(IllegalStateException.class);
        parser.queryByQuery("not jsoup doc", new HashMap<>());
    }

    @Test
    public void testJSoupHtmlParser() {

    }

    private Member createTestMember() {
        Fields fields = new XOBuilder<Fields>()
                .add("<xf:indexRange value='2-3' />").buildFields();

        Axis col = new Axis();
        col.setName(AxisName.COL);
        col.setIndex(1);
        col.setOrder(1);
        col.setFields(fields);

        Member member = new Member();
        member.addAxis(col);

        return member;
    }

    private DataDef createTestDataDef() {
        //@formatter:off
        DataDef dataDef = new XOBuilder<DataDef>()
            .add("<dataDef name='dd' >")
            .add("  <axis name='col' >")
            .add("    <xf:fields>")
            .add("      <xf:query region='div#test' field='p' />")
            .add("    </xf:fields>")
            .add("  </axis>")
            .add("</dataDef>")
            .build(DataDef.class).get(0);
        //@formatter:on

        return dataDef;
    }

}
