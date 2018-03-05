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
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.helper.URLConnectionHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Locator;
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
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    private URLConnectionHelper ucHelper;

    @Mock
    private ActivityService activityService;
    @Mock
    private ConfigService configService;
    @Mock
    private FieldsHelper fieldsHelper;

    private Locator locator;
    private String resourceUrl;
    private Labels labels;

    @InjectMocks
    private URLLoader loader;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        locator = createTestLocator();
        loader.setInput(locator);
        FieldUtils.writeField(loader, "document", locator.getDocuments().get(0),
                true);

        labels = new Labels("x", "y");
        loader.setLabels(labels);

        resourceUrl = "/testdefs/datadefservice/valid-v1/bean.xml";
    }

    @Test
    public void testInitialize() throws IllegalAccessException {
        // when
        boolean actual = loader.initialize();

        assertThat(actual).isTrue();
    }

    @Test
    public void testInitializeIllegalState() throws IllegalAccessException {
        // step input is null
        FieldUtils.writeField(loader, "locator", null, true);

        // step input is not set
        testRule.expect(IllegalStateException.class);
        loader.initialize();
    }

    @Test
    public void testLoadSavedLocator() throws IllegalAccessException {
        locator.setUrl(resourceUrl);

        Locator savedLocator = createTestLocator();

        given(locatorPersistence.loadLocator("n", "g"))
                .willReturn(savedLocator);

        // when
        boolean actual = loader.load();

        Locator loadedLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.LOAD);
        assertThat(actual).isTrue();

        assertThat(loadedLocator).isSameAs(savedLocator);
        assertThat(loadedLocator.getUrl()).isEqualTo(resourceUrl);
        assertThat(savedLocator.getUrl()).isEqualTo(resourceUrl);

        assertThat(loadedLocator.getFields().getNodes().size()).isEqualTo(1);
        assertThat(savedLocator.getFields().getNodes().size()).isEqualTo(1);
        assertThat(savedLocator.getFields().getNodes())
                .containsAll(loadedLocator.getFields().getNodes());
    }

    @Test
    public void testLoadNoSavedLocator() throws IllegalAccessException {

        given(locatorPersistence.loadLocator("n", "g")).willReturn(null);

        // when
        boolean actual = loader.load();

        Locator loadedLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.LOAD);
        assertThat(actual).isTrue();

        assertThat(loadedLocator).isSameAs(locator);
    }

    @Test
    public void testLoadIllegalState() throws IllegalAccessException {
        // step input is null
        FieldUtils.writeField(loader, "locator", null, true);

        testRule.expect(IllegalStateException.class);
        loader.load();
    }

    @Test
    public void testProcessLocatorWithId() throws IllegalAccessException {
        locator.setId(1L);
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);

        Document document = locator.getDocuments().get(0);
        Long documentId = document.getId();
        document.setFromDate(fromDate);
        document.setToDate(toDate);

        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(documentId);
        given(documentPersistence.loadDocument(documentId))
                .willReturn(document);
        given(documentHelper.getDocument(documentId, locator.getDocuments()))
                .willReturn(document);
        given(documentHelper.getToDate(document.getFromDate(),
                locator.getFields(), labels)).willReturn(document.getToDate());
        given(configService.getRunDateTime()).willReturn(fromDate);

        // when
        boolean actual = loader.process();

        Document loadedDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(loadedDocument).isSameAs(document);

        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();
    }

    @Test
    public void testProcessLocatorWithoutId() throws IllegalAccessException {
        // when locator is without id, new document is created

        locator.setUrl(resourceUrl);

        Document document = locator.getDocuments().get(0);

        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        document.setName(locator.getName());
        document.setUrl(locator.getUrl());
        document.setFromDate(fromDate);
        document.setToDate(toDate);

        given(ucHelper.getProtocol(resourceUrl)).willReturn("resource");
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, locator.getFields(), labels))
                .willReturn(toDate);
        given(documentHelper.createDocument(document.getName(),
                document.getUrl(), fromDate, toDate)).willReturn(document);

        // when
        boolean actual = loader.process();

        Document createdDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(createdDocument).isSameAs(document);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        verify(documentHelper, never())
                .getActiveDocumentId(locator.getDocuments());
        verifyZeroInteractions(documentPersistence);
    }

    @Test
    public void testProcessWithActiveDocument() throws IllegalAccessException {
        locator.setId(1L);
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);

        Document document = locator.getDocuments().get(0);
        Long documentId = document.getId();
        document.setFromDate(fromDate);
        document.setToDate(toDate);

        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(documentId);
        given(documentPersistence.loadDocument(documentId))
                .willReturn(document);
        given(documentHelper.getDocument(documentId, locator.getDocuments()))
                .willReturn(document);
        given(documentHelper.getToDate(document.getFromDate(),
                locator.getFields(), labels)).willReturn(document.getToDate());
        given(configService.getRunDateTime()).willReturn(fromDate);

        // when
        boolean actual = loader.process();

        Document loadedDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(loadedDocument).isEqualTo(document);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();
        assertThat(loader.isConsistent()).isTrue();
    }

    @Test
    public void testProcessWithActiveDocumentExpiredForNewLive()
            throws IllegalAccessException, IOException {
        locator.setUrl(resourceUrl);
        locator.setId(1L);

        List<Document> documents = locator.getDocuments();
        Document activeDocument = documents.get(0);
        Long documentId = activeDocument.getId();
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -3);
        Date toDate = DateUtils.addDays(runDate, 2);
        // new toDate is less then old toDate
        Date newToDate = DateUtils.addDays(runDate, -1);

        String name = locator.getName();
        String url = locator.getUrl();
        fromDate = runDate;
        toDate = DateUtils.addDays(runDate, 1);
        Document createdDocument = new Document();
        createdDocument.setName(name);
        createdDocument.setUrl(url);
        createdDocument.setFromDate(fromDate);
        createdDocument.setToDate(toDate);

        given(ucHelper.getProtocol(url)).willReturn("resource");
        byte[] docObject = loader.fetchDocumentObject(resourceUrl);

        FieldUtils.writeField(loader, "locator", locator, true);

        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(documentId);
        given(documentHelper.getDocument(documentId, locator.getDocuments()))
                .willReturn(activeDocument);

        given(documentHelper.getToDate(activeDocument.getFromDate(),
                locator.getFields(), labels)).willReturn(newToDate);

        given(documentHelper.getToDate(createdDocument.getFromDate(),
                locator.getFields(), labels))
                        .willReturn(createdDocument.getToDate());
        given(documentHelper.createDocument(name, url, fromDate, toDate))
                .willReturn(createdDocument);

        // when
        boolean actual = loader.process();

        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualDocument).isSameAs(createdDocument);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        assertThat(createdDocument.getName()).isEqualTo(locator.getName());
        assertThat(createdDocument.getFromDate()).isEqualTo(fromDate);
        assertThat(createdDocument.getToDate()).isEqualTo(toDate);
        assertThat(createdDocument.getUrl()).isEqualTo(resourceUrl);

        assertThat(locator.getDocuments().size()).isEqualTo(2);
        assertThat(locator.getDocuments()).contains(activeDocument);
        assertThat(locator.getDocuments()).contains(createdDocument);
        assertThat(loader.isConsistent()).isTrue();

        verify(documentHelper).setDocumentObject(createdDocument, docObject);
        verifyZeroInteractions(documentPersistence);
    }

    @Test
    public void testProcessWithoutActiveDocument()
            throws IllegalAccessException, IOException {
        locator.setUrl(resourceUrl);

        List<Document> documents = locator.getDocuments();
        Document existingDocument = documents.get(0);

        String name = locator.getName();
        String url = locator.getUrl();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        Document createdDocument = new Document();
        createdDocument.setName(name);
        createdDocument.setUrl(url);
        createdDocument.setFromDate(fromDate);
        createdDocument.setToDate(toDate);

        given(ucHelper.getProtocol(resourceUrl)).willReturn("resource");
        byte[] docObject = loader.fetchDocumentObject(resourceUrl);

        FieldUtils.writeField(loader, "locator", locator, true);

        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(null);
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, locator.getFields(), labels))
                .willReturn(toDate);
        given(documentHelper.createDocument(name, url, fromDate, toDate))
                .willReturn(createdDocument);

        // when
        boolean actual = loader.process();

        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualDocument).isSameAs(createdDocument);
        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        assertThat(createdDocument.getName()).isEqualTo(locator.getName());
        assertThat(createdDocument.getFromDate()).isEqualTo(fromDate);
        assertThat(createdDocument.getToDate()).isEqualTo(toDate);
        assertThat(createdDocument.getUrl()).isEqualTo(resourceUrl);

        assertThat(locator.getDocuments().size()).isEqualTo(2);
        assertThat(locator.getDocuments()).contains(existingDocument);
        assertThat(locator.getDocuments()).contains(createdDocument);
        assertThat(loader.isConsistent()).isTrue();

        verify(documentHelper).setDocumentObject(createdDocument, docObject);
        verifyZeroInteractions(documentPersistence);
    }

    @Test
    public void testProcessFetchDocExpectException()
            throws IllegalAccessException {
        String url = "file:///xyz";
        locator.setUrl(url); // unknown file

        given(ucHelper.getProtocol(url)).willReturn("file");

        // when
        try {
            loader.process();
            fail("should throw StepRunException");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }

        testRule.expect(StepRunException.class);
        loader.process();
    }

    @Test
    public void testProcessSetDocumentExpectException()
            throws IllegalAccessException, IOException {

        locator.setUrl(resourceUrl);

        String name = locator.getName();
        String url = locator.getUrl();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        Document newDocument = new Document();
        newDocument.setName(name);
        newDocument.setUrl(url);
        newDocument.setFromDate(fromDate);
        newDocument.setToDate(toDate);

        given(ucHelper.getProtocol(resourceUrl)).willReturn("resource");
        given(documentHelper.getActiveDocumentId(locator.getDocuments()))
                .willReturn(null);
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, locator.getFields(), labels))
                .willReturn(toDate);
        given(documentHelper.createDocument(name, url, fromDate, toDate))
                .willReturn(newDocument);

        given(documentHelper.setDocumentObject(any(Document.class),
                any(byte[].class))).willThrow(IOException.class);

        // when
        try {
            loader.process();
            fail("should throw StepRunException");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }

        testRule.expect(StepRunException.class);
        loader.process();
    }

    @Test
    public void testProcessIllegalState() throws IllegalAccessException {
        // step input is null
        FieldUtils.writeField(loader, "locator", null, true);

        testRule.expect(IllegalStateException.class);
        loader.process();
    }

    @Test
    public void testStoreWhenLocatorNotStored()
            throws IllegalAccessException, FieldsException {

        given(locatorPersistence.storeLocator(locator)).willReturn(false);
        // when
        boolean actual = loader.store();

        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);
        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        assertThat(actualLocator).isSameAs(locator);
        assertThat(actualDocument).isSameAs(locator.getDocuments().get(0));

        verify(documentHelper, never())
                .getActiveDocumentId(locator.getDocuments());

        verify(locatorPersistence, never()).loadLocator(any(Long.class));
        verifyZeroInteractions(documentPersistence);

    }

    @Test
    public void testStoreWhenLocatorStored()
            throws IllegalAccessException, FieldsException {

        locator.setId(1L);

        Locator storedLocator = createTestLocator();
        Document storedDocument = storedLocator.getDocuments().get(0);

        given(locatorPersistence.storeLocator(locator)).willReturn(true);
        given(locatorPersistence.loadLocator(locator.getId()))
                .willReturn(storedLocator);
        given(documentPersistence
                .loadDocument(locator.getDocuments().get(0).getId()))
                        .willReturn(storedDocument);

        // when
        boolean actual = loader.store();

        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        assertThat(actualLocator).isSameAs(storedLocator);
        assertThat(actualDocument).isSameAs(storedDocument);
    }

    @Test
    public void testStoreWhenLoadedNulls()
            throws IllegalAccessException, FieldsException {

        locator.setId(1L);

        given(locatorPersistence.storeLocator(locator)).willReturn(true);
        given(locatorPersistence.loadLocator(locator.getId())).willReturn(null);
        given(documentPersistence
                .loadDocument(locator.getDocuments().get(0).getId()))
                        .willReturn(null);

        // when
        boolean actual = loader.store();

        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        assertThat(actualLocator).isSameAs(locator);
        assertThat(actualDocument).isSameAs(locator.getDocuments().get(0));
    }

    @Test
    public void testStoreExpectException() throws IllegalAccessException {
        locator.setId(0L);

        given(locatorPersistence.storeLocator(locator)).willReturn(true);
        given(locatorPersistence.loadLocator(locator.getId()))
                .willThrow(RuntimeException.class);

        testRule.expect(StepRunException.class);
        loader.store();
    }

    @Test
    public void testStoreIllegalState() throws IllegalAccessException {
        FieldUtils.writeField(loader, "locator", null, true);
        FieldUtils.writeField(loader, "document", null, true);

        try {
            loader.store();
            fail("must throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("step input [locator] must not be null");
        }

        try {
            loader.setInput(new Locator());
            loader.store();
            fail("must throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("document must not be null");
        }
    }

    @Test
    public void testHandoverTaskFieldsError()
            throws IllegalAccessException, FieldsException {

        Fields fields = TestUtil.createEmptyFields();

        locator.setFields(fields);

        given(fieldsHelper.split("/xf:fields/xf:tasks/xf:task",
                locator.getFields())).willThrow(FieldsException.class);

        try {
            loader.handover();
            fail("should throw StepRunException");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(FieldsException.class);
        }
    }

    @Test
    public void testHandoverNoTasks()
            throws IllegalAccessException, FieldsException {

        List<Fields> tasks = new ArrayList<>();
        given(fieldsHelper.split("/xf:fields/xf:tasks/xf:task",
                locator.getFields())).willReturn(tasks);

        try {
            loader.handover();
            fail("should throw StepRunException");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(FieldsException.class);
        }
    }

    @Test
    public void testHandoverDocumentNotLoaded()
            throws IllegalAccessException, FieldsException {

        // unload document
        FieldUtils.writeField(loader, "document", null, true);

        List<Fields> tasks = new ArrayList<>();
        tasks.add(new Fields());
        tasks.add(new Fields());
        tasks.add(new Fields());

        given(fieldsHelper.split("/xf:fields/xf:tasks/xf:task",
                locator.getFields())).willReturn(tasks);

        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        verify(activityService, times(3)).addActivity(eq(Type.FAIL),
                any(String.class));
        verifyZeroInteractions(stepService);
    }

    @Test
    public void testHandover() throws IllegalAccessException, FieldsException {

        Document document = locator.getDocuments().get(0);
        FieldUtils.writeField(loader, "document", document, true);

        FieldsHelper fh = new FieldsHelper();
        List<Fields> tasks =
                fh.split("/xf:fields/xf:tasks/xf:task", locator.getFields());
        Fields task1 = fh.deepCopy(tasks.get(0));
        Fields task2 = fh.deepCopy(tasks.get(1));

        given(fieldsHelper.split("/xf:fields/xf:tasks/xf:task",
                locator.getFields())).willReturn(tasks);
        given(fieldsHelper.deepCopy(tasks.get(0))).willReturn(task1);
        given(fieldsHelper.deepCopy(tasks.get(1))).willReturn(task2);

        // when
        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        InOrder inOrder = inOrder(stepService, fieldsHelper);
        inOrder.verify(fieldsHelper).deepCopy(tasks.get(0));
        inOrder.verify(stepService).pushTask(loader, document, labels, task1);
        inOrder.verify(fieldsHelper).deepCopy(tasks.get(1));
        inOrder.verify(stepService).pushTask(loader, document, labels, task2);
    }

    @Test
    public void testHandoverNextStepFieldsException()
            throws IllegalAccessException, FieldsException {
        Document document = locator.getDocuments().get(0);
        FieldUtils.writeField(loader, "document", document, true);

        FieldsHelper fh = new FieldsHelper();
        List<Fields> tasks =
                fh.split("/xf:fields/xf:tasks/xf:task", locator.getFields());

        given(fieldsHelper.split("/xf:fields/xf:tasks/xf:task",
                locator.getFields())).willReturn(tasks);

        given(fieldsHelper.deepCopy(tasks.get(0)))
                .willThrow(FieldsException.class);
        given(fieldsHelper.deepCopy(tasks.get(1)))
                .willThrow(FieldsException.class);

        // when

        loader.handover();

        verify(activityService, times(2)).addActivity(eq(Type.FAIL),
                any(String.class));
    }

    @Test
    public void testHandoverIllegalState() throws IllegalAccessException {
        // step input null
        FieldUtils.writeField(loader, "locator", null, true);

        testRule.expect(IllegalStateException.class);
        loader.handover();
    }

    @Test
    public void testIsConsistent() throws IllegalAccessException {
        boolean actual = loader.isConsistent();
        assertThat(actual).isFalse();

        loader.setConsistent(true);

        FieldUtils.writeField(loader, "document", null, true);
        actual = loader.isConsistent();
        assertThat(actual).isFalse();

        FieldUtils.writeField(loader, "document", new Document(), true);

        actual = loader.isConsistent();
        assertThat(actual).isTrue();
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        Locator expected = new Locator();
        loader.setInput(expected);
        Locator actual =
                (Locator) FieldUtils.readField(loader, "locator", true);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    public void testSetInputShouldThrowException()
            throws IllegalAccessException {
        FieldUtils.writeField(loader, "locator", null, true);
        testRule.expect(StepRunException.class);
        loader.setInput("xyz");
    }

    private Locator createTestLocator() {

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add(" <xf:tasks>")
          .add("  <xf:task>a</xf:task>")
          .add("  <xf:task>b</xf:task>")
          .add(" </xf:tasks>")
          .buildFields();
        //@formatter:on

        Locator testLocator = new Locator();
        testLocator.setFields(fields);
        testLocator.setName("n");
        testLocator.setGroup("g");
        testLocator.setUrl("u");

        Document testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setName("d");
        testLocator.getDocuments().add(testDocument);

        return testLocator;
    }

}
