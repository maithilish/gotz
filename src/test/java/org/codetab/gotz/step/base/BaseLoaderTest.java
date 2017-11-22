package org.codetab.gotz.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.extract.URLLoader;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XFieldBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Marker;
import org.w3c.dom.Node;

/**
 * <p>
 * BaseLoader tests.
 * @author Maithilish
 *
 */
public class BaseLoaderTest {

    @Mock
    private LocatorPersistence locatorPersistence;
    @Mock
    private DocumentPersistence documentPersistence;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private StepService stepService;
    @Mock
    private DInjector dInjector;

    @Mock
    private ActivityService activityService;
    @Mock
    private ConfigService configService;
    @Spy
    private FieldsHelper xFieldHelper;

    private String fileUrl;

    @InjectMocks
    private URLLoader loader;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        fileUrl =
                "target/test-classes/testdefs/datadefservice/valid-v1/bean.xml";
    }

    @Test
    public void testInitialize() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        loader.setInput(locator);

        // when
        boolean actual = loader.initialize();

        Marker marker = (Marker) FieldUtils.readField(loader, "marker", true);

        assertThat(actual).isTrue();
        assertThat(marker.getName()).isEqualTo("LOG_L1_G1");
    }

    @Test
    public void testInitializeIllegalState() {
        // step input is not set
        testRule.expect(IllegalStateException.class);
        loader.initialize();
    }

    @Test
    public void testLoadSavedLocator() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator newLocator = locators.get(0);
        Locator savedLocator = locators.get(1);

        newLocator.setUrl(fileUrl);

        loader.setInput(newLocator);
        given(locatorPersistence.loadLocator("l1", "g1"))
                .willReturn(savedLocator);

        // when
        boolean actual = loader.load();

        Locator locator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.LOAD);
        assertThat(actual).isTrue();

        assertThat(locator).isSameAs(savedLocator);
        assertThat(newLocator.getUrl()).isEqualTo(fileUrl);
        assertThat(savedLocator.getUrl()).isEqualTo(fileUrl);

        assertThat(newLocator.getXField().getNodes().size()).isEqualTo(1);
        assertThat(savedLocator.getXField().getNodes().size()).isEqualTo(1);
        assertThat(savedLocator.getXField().getNodes())
                .containsAll(newLocator.getXField().getNodes());
    }

    @Test
    public void testLoadNoSavedLocator() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator newLocator = locators.get(0);

        newLocator.setUrl(fileUrl);

        loader.setInput(newLocator);
        // load from db returns null
        given(locatorPersistence.loadLocator("l1", "g1")).willReturn(null);

        // when
        boolean actual = loader.load();

        Locator locator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.LOAD);
        assertThat(actual).isTrue();

        assertThat(locator).isSameAs(newLocator);
        assertThat(newLocator.getUrl()).isEqualTo(fileUrl);
        assertThat(newLocator.getXField().getNodes().size()).isEqualTo(1);
    }

    @Test
    public void testLoadIllegalState() {
        // step input is not set
        testRule.expect(IllegalStateException.class);
        loader.load();
    }

    @Test
    public void testProcessLocatorWithId() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setId(1L);

        List<Document> documents = locator.getDocuments();
        Document document = documents.get(0);
        Long documentId = document.getId();

        given(documentHelper.getActiveDocumentId(documents))
                .willReturn(documentId);
        given(documentPersistence.loadDocument(documentId))
                .willReturn(document);

        FieldUtils.writeField(loader, "locator", locator, true);

        // when
        boolean actual = loader.process();

        Document actualDoc =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualDoc).isEqualTo(document);

        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        // Redundant
        // verify(documentHelper).getActiveDocumentId(documents);
        // verify(documentPersistence).loadDocument(documentId);
    }

    @Test
    public void testProcessLocatorWithoutId() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setUrl(fileUrl);
        locator.setXField(new XField());

        List<Document> documents = locator.getDocuments();
        Document document = documents.get(0);

        String name = locator.getName();
        String url = locator.getUrl();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        document.setName(name);
        document.setUrl(url);
        document.setFromDate(fromDate);
        document.setToDate(toDate);

        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, locator.getXField()))
                .willReturn(toDate);
        given(documentHelper.createDocument(name, url, fromDate, toDate))
                .willReturn(document);

        FieldUtils.writeField(loader, "locator", locator, true);

        // when
        boolean actual = loader.process();

        Document actualDoc =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualDoc).isEqualTo(document);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        verify(documentHelper, never()).getActiveDocumentId(documents);
        verifyZeroInteractions(documentPersistence);
    }

    @Test
    public void testProcessWithActiveDocument() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setId(1L);
        List<Document> documents = locator.getDocuments();
        Document document = documents.get(0);
        Long documentId = document.getId();

        given(documentHelper.getActiveDocumentId(documents))
                .willReturn(documentId);
        given(documentPersistence.loadDocument(documentId))
                .willReturn(document);

        FieldUtils.writeField(loader, "locator", locator, true);

        // when
        boolean actual = loader.process();

        Document actualDoc =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualDoc).isEqualTo(document);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();
        assertThat(loader.isConsistent()).isTrue();

        // Redundant
        // verify(documentPersistence).loadDocument(documentId);
    }

    @Test
    public void testProcessWithoutActiveDocument()
            throws IllegalAccessException, IOException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setUrl(fileUrl);
        locator.setXField(new XField());

        List<Document> documents = locator.getDocuments();
        Document existingDocument = documents.get(0);

        String name = locator.getName();
        String url = locator.getUrl();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        Document newDocument = new Document();
        newDocument.setName(name);
        newDocument.setUrl(url);
        newDocument.setFromDate(fromDate);
        newDocument.setToDate(toDate);

        byte[] docObject = loader.fetchDocumentObject(fileUrl);

        FieldUtils.writeField(loader, "locator", locator, true);

        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(null);
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, locator.getXField()))
                .willReturn(toDate);
        given(documentHelper.createDocument(name, url, fromDate, toDate))
                .willReturn(newDocument);

        // when
        boolean actual = loader.process();

        Document actualDoc =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualDoc).isEqualTo(newDocument);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        assertThat(newDocument.getName()).isEqualTo(locator.getName());
        assertThat(newDocument.getFromDate()).isEqualTo(fromDate);
        assertThat(newDocument.getToDate()).isEqualTo(toDate);
        assertThat(newDocument.getUrl()).isEqualTo(fileUrl);

        assertThat(locator.getDocuments().size()).isEqualTo(2);
        assertThat(locator.getDocuments()).contains(existingDocument);
        assertThat(locator.getDocuments()).contains(newDocument);
        assertThat(loader.isConsistent()).isTrue();

        verify(documentHelper).setDocumentObject(newDocument, docObject);
        verifyZeroInteractions(documentPersistence);
    }

    @Test
    public void testProcessFetchDocExpectException()
            throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        FieldUtils.writeField(loader, "locator", locator, true);

        // when
        try {
            loader.process();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(IOException.class));
        }

        testRule.expect(StepRunException.class);
        loader.process();
    }

    @Test
    public void testProcessSetDocumentExpectException()
            throws IllegalAccessException, IOException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setUrl(fileUrl);
        locator.setXField(new XField());

        String name = locator.getName();
        String url = locator.getUrl();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        Document newDocument = new Document();
        newDocument.setName(name);
        newDocument.setUrl(url);
        newDocument.setFromDate(fromDate);
        newDocument.setToDate(toDate);

        FieldUtils.writeField(loader, "locator", locator, true);

        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(null);
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, locator.getXField()))
                .willReturn(toDate);
        given(documentHelper.createDocument(name, url, fromDate, toDate))
                .willReturn(newDocument);

        given(documentHelper.setDocumentObject(any(Document.class),
                any(byte[].class))).willThrow(IOException.class);

        // when
        try {
            loader.process();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(IOException.class));
        }

        testRule.expect(StepRunException.class);
        loader.process();
    }

    @Test
    public void testProcessIllegalState() {
        // step input is not set
        testRule.expect(IllegalStateException.class);
        loader.process();
    }

    @Test
    public void testStorePersistIsFalse()
            throws IllegalAccessException, FieldsException {
        XField xField = xFieldHelper.createXField();
        Node tasks = xFieldHelper.addElement("tasks", "", xField);
        Node persist = xFieldHelper.addElement("persist", "", tasks);
        xFieldHelper.addElement("document", "false", persist);

        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        Document document = locator.getDocuments().get(0);
        locator.setXField(xField);

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        // when
        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        verifyZeroInteractions(documentPersistence);
        verifyZeroInteractions(locatorPersistence);
    }

    @Test
    public void testStorePersistIsTrue()
            throws IllegalAccessException, FieldsException {
        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        locator1.setId(0L);
        Locator locator2 = locators.get(1);
        locator2.setId(0L);
        Document document1 = locator1.getDocuments().get(0);
        Document document2 = new Document();

        FieldUtils.writeField(loader, "locator", locator1, true);
        FieldUtils.writeField(loader, "document", document1, true);

        XField xField = xFieldHelper.createXField();
        Node tasks = xFieldHelper.addElement("tasks", "", xField);
        Node persist = xFieldHelper.addElement("persist", "", tasks);
        xFieldHelper.addElement("document", "true", persist);
        locator1.setXField(xField);

        given(locatorPersistence.loadLocator(locator1.getId()))
                .willReturn(locator2);
        given(documentPersistence.loadDocument(document1.getId()))
                .willReturn(document2);

        // when
        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        assertThat(locator2.getXField().getNodes())
                .containsAll(locator1.getXField().getNodes());

        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualLocator).isSameAs(locator2);
        assertThat(actualDocument).isSameAs(document2);

        verify(locatorPersistence).storeLocator(locator1);
    }

    @Test
    public void testStorePersistUndefined() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        locator1.setId(0L);
        Locator locator2 = locators.get(1);
        locator2.setId(0L);
        Document document1 = locator1.getDocuments().get(0);
        Document document2 = new Document();

        XField xField = new XField();
        locator1.setXField(xField);
        locator2.setXField(xField);

        FieldUtils.writeField(loader, "locator", locator1, true);
        FieldUtils.writeField(loader, "document", document1, true);

        given(locatorPersistence.loadLocator(locator1.getId()))
                .willReturn(locator2);
        given(documentPersistence.loadDocument(document1.getId()))
                .willReturn(document2);

        // when
        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        assertThat(locator2.getXField().getNodes())
                .containsAll(locator1.getXField().getNodes());

        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualLocator).isSameAs(locator2);
        assertThat(actualDocument).isSameAs(document2);

        verify(locatorPersistence).storeLocator(locator1);
    }

    @Test
    public void testStoreExpectException() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setId(0L);
        locator.setXField(new XField());

        Document document = locator.getDocuments().get(0);

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        given(locatorPersistence.loadLocator(locator.getId()))
                .willThrow(RuntimeException.class);

        // when
        try {
            loader.store();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(RuntimeException.class));
        }

        testRule.expect(StepRunException.class);
        loader.store();
    }

    @Test
    public void testStoreIllegalState() {
        try {
            // step input is not set
            loader.store();
            fail("must throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("step input [locator] must not be null");
        }

        try {
            // document is null
            loader.setInput(new Locator());
            loader.store();
            fail("must throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("document must not be null");
        }
    }

    @Test
    public void testHandoverNoDataDefFields() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setXField(TestUtil.buildXField("", "xf"));

        FieldUtils.writeField(loader, "locator", locator, true);

        try {
            loader.handover();
            fail("should throw StepRunExpection");
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldsException.class));
        }
    }

    @Test
    public void testHandoverDocumentNotLoaded()
            throws IllegalAccessException, FieldsException {

        XField xField = xFieldHelper.createXField();
        Node tasks = xFieldHelper.addElement("tasks", "", xField);
        xFieldHelper.addElement("task", "x", tasks);
        xFieldHelper.addElement("task", "y", tasks);

        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setXField(xField);

        FieldUtils.writeField(loader, "locator", locator, true);

        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        verify(activityService, times(2)).addActivity(eq(Type.GIVENUP),
                any(String.class));
        verifyZeroInteractions(stepService);
    }

    @Test
    public void testHandover() throws IllegalAccessException, FieldsException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.getXField().getNodes().clear();
        Document document = locator.getDocuments().get(0);

        XField xField = xFieldHelper.createXField();
        Node tasksNode = xFieldHelper.addElement("tasks", "", xField);
        xFieldHelper.addElement("task", "a", tasksNode);
        xFieldHelper.addElement("task", "b", tasksNode);

        locator.setXField(xField);

        List<XField> tasks =
                xFieldHelper.split("/:xfield/:tasks/:task", xField);
        XField task1 = tasks.get(0);
        XField task2 = tasks.get(1);

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        given(xFieldHelper.split("/:xfield/:tasks/:task", xField))
                .willReturn(tasks);
        given(xFieldHelper.deepCopy(task1)).willReturn(task1);
        given(xFieldHelper.deepCopy(task2)).willReturn(task2);

        // when
        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        InOrder inOrder = inOrder(stepService);
        inOrder.verify(stepService).pushTask(loader, document, task1);
        inOrder.verify(stepService).pushTask(loader, document, task2);
    }

    @Test
    public void testHandoverNextStepFieldsException()
            throws IllegalAccessException, FieldsException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.getXField().getNodes().clear();
        Document document = locator.getDocuments().get(0);

        XField xField = new XField();
        locator.setXField(xField);

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        // when
        try {
            loader.handover();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldsException.class));
        }
    }

    @Test
    public void testHandoverIllegalState() {
        // step input is not set
        testRule.expect(IllegalStateException.class);
        loader.handover();
    }

    @Test
    public void testIsConsistent() throws IllegalAccessException {
        boolean actual = loader.isConsistent();
        assertThat(actual).isFalse();

        loader.setConsistent(true);

        actual = loader.isConsistent();
        assertThat(actual).isFalse();

        Document document = new Document();
        FieldUtils.writeField(loader, "document", document, true);

        actual = loader.isConsistent();
        assertThat(actual).isTrue();
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        loader.setInput("xyz");
        Locator actual =
                (Locator) FieldUtils.readField(loader, "locator", true);
        assertThat(actual).isNull();

        Locator locator = new Locator();
        loader.setInput(locator);
        actual = (Locator) FieldUtils.readField(loader, "locator", true);
        assertThat(actual).isSameAs(locator);
    }

    private List<Locator> createTestObjects() {

        //@formatter:off
        XField l1f = new XFieldBuilder()
                .add("<l1f>")
                .add(" <l1f1>l1v1</l1f1>")
                .add(" <l1f2>l1v2</l1f2>")
                .add("</l1f>")
                .build("xf");
        //@formatter:on

        Locator locator1 = new Locator();
        locator1.setXField(l1f);
        locator1.setName("l1");
        locator1.setGroup("g1");
        locator1.setUrl("url1");

      //@formatter:off
        XField l2f = new XFieldBuilder()
                .add("<l2f>")
                .add(" <l2f1>l2v1</l2f1>")
                .add(" <l2f2>l2v2</l2f2>")
                .add("</l2f>")
                .build("xf");
        //@formatter:on

        Locator locator2 = new Locator();
        locator1.setXField(l2f);
        locator2.setName("l1");
        locator2.setGroup("g1");
        locator2.setUrl("url2");

        List<Locator> locators = new ArrayList<>();
        locators.add(locator1);
        locators.add(locator2);

        Document document = new Document();
        document.setId(1L);
        document.setName("d1");
        locator1.getDocuments().add(document);

        return locators;
    }

}
