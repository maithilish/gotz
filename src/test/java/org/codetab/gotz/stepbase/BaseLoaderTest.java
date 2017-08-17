package org.codetab.gotz.stepbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.DocumentHelper;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.steps.URLLoader;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

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
    private FieldsHelper fieldsHelper;
    @Mock
    private StepService stepService;
    @Mock
    private DInjector dInjector;

    @Mock
    private ActivityService activityService;
    @Mock
    private ConfigService configService;

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

        assertThat(newLocator.getFields().size()).isEqualTo(1);
        assertThat(savedLocator.getFields().size()).isEqualTo(2);
        assertThat(savedLocator.getFields())
                .containsAll(newLocator.getFields());
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
        assertThat(newLocator.getFields().size()).isEqualTo(1);
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
        given(documentHelper.getToDate(fromDate, locator.getFields()))
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
        given(documentHelper.getToDate(fromDate, locator.getFields()))
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
        given(documentHelper.getToDate(fromDate, locator.getFields()))
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
    public void testStorePersistIsFalse() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        Document document = locator.getDocuments().get(0);

        Field field = TestUtil.createField("document", "false");
        Fields fields = TestUtil.createFields("group", "persist", field);
        locator.getFields().add(fields);

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
    public void testStorePersistIsTrue() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        locator1.setId(0L);
        Locator locator2 = locators.get(1);
        locator2.setId(0L);
        Document document1 = locator1.getDocuments().get(0);
        Document document2 = new Document();

        FieldUtils.writeField(loader, "locator", locator1, true);
        FieldUtils.writeField(loader, "document", document1, true);

        Field field = TestUtil.createField("persist", "true");
        locator1.getFields().add(field);

        given(locatorPersistence.loadLocator(locator1.getId()))
                .willReturn(locator2);
        given(documentPersistence.loadDocument(document1.getId()))
                .willReturn(document2);

        // when
        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        assertThat(locator2.getFields()).containsAll(locator1.getFields());

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

        assertThat(locator2.getFields()).containsAll(locator1.getFields());

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

        FieldUtils.writeField(loader, "locator", locator, true);

        try {
            loader.handover();
            fail("should throw StepRunExpection");
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldNotFoundException.class));
        }
    }

    @Test
    public void testHandoverDocumentNotLoaded() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        Field dataDef1 = TestUtil.createField("datadef", "d1");
        Field f1 = TestUtil.createField("f1", "x1");
        Fields dataDef2 = TestUtil.createFields("datadef", "d2", f1);
        Fields dataDefs =
                TestUtil.createFields("group", "datadef", dataDef1, dataDef2);
        locator.getFields().add(dataDefs);

        FieldUtils.writeField(loader, "locator", locator, true);

        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        verify(activityService).addActivity(eq(Type.GIVENUP),
                any(String.class));
        verifyZeroInteractions(stepService);
    }

    @Test
    public void testHandover() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.getFields().clear();
        Document document = locator.getDocuments().get(0);

        Field f1 = TestUtil.createField("f1", "x1");
        Field f2 = TestUtil.createField("f2", "x2");
        Fields dataDef1 = TestUtil.createFields("datadef", "d1", f1);
        Fields dataDef2 = TestUtil.createFields("datadef", "d2", f2);
        locator.getFields()
                .add(TestUtil.createFields("group", "datadef", dataDef1));
        locator.getFields()
                .add(TestUtil.createFields("group", "datadef", dataDef2));

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        // nextStepFields for task 1 and 2
        FieldsBase locatorNameField =
                TestUtil.createField("locatorName", locator.getName());
        FieldsBase locatorGroupField =
                TestUtil.createField("locatorGroup", locator.getGroup());
        FieldsBase locatorUrlField =
                TestUtil.createField("locatorUrl", locator.getUrl());

        // task 1 Fields
        List<FieldsBase> task1Fields = new ArrayList<>();
        Fields dd1 = TestUtil.createFields("group", "datadef", dataDef1);
        task1Fields.add(dd1);
        task1Fields.add(locatorNameField);
        task1Fields.add(locatorGroupField);
        task1Fields.add(locatorUrlField);

        // task 2 Fields
        List<FieldsBase> task2Fields = new ArrayList<>();
        Fields dd2 = TestUtil.createFields("group", "datadef", dataDef2);
        task2Fields.add(dd2);
        task2Fields.add(locatorNameField);
        task2Fields.add(locatorGroupField);
        task2Fields.add(locatorUrlField);

        given(fieldsHelper.deepClone(dd1)).willReturn(dd1);
        given(fieldsHelper.deepClone(dd2)).willReturn(dd2);

        // when
        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        InOrder inOrder = inOrder(stepService);
        inOrder.verify(stepService).pushTask(loader, document, task1Fields);
        inOrder.verify(stepService).pushTask(loader, document, task2Fields);
    }

    @Test
    public void testHandoverNextStepFieldsException()
            throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.getFields().clear();
        Document document = locator.getDocuments().get(0);

        Field f1 = TestUtil.createField("f1", "x1");

        Fields dataDef1 = TestUtil.createFields("datadef", "d1", f1);
        Fields dd1 = TestUtil.createFields("group", "datadef", dataDef1);
        locator.getFields().add(dd1);

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        given(fieldsHelper.deepClone(dd1)).willThrow(RuntimeException.class);

        // when
        loader.handover();

        // should log activity
        verify(activityService).addActivity(eq(Type.GIVENUP),
                any(String.class));
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
        Field l1f1 = TestUtil.createField("l1f1", "l1v1");
        Field l1f2 = TestUtil.createField("l1f2", "l1v2");
        Fields l1f = TestUtil.createFields("l1f", "l1v", l1f1, l1f2);

        Locator locator1 = new Locator();
        locator1.getFields().add(l1f);
        locator1.setName("l1");
        locator1.setGroup("g1");
        locator1.setUrl("url1");

        Field l2f1 = TestUtil.createField("l2f1", "l2v1");
        Field l2f2 = TestUtil.createField("l2f2", "l2v2");
        Fields l2f = TestUtil.createFields("l2f", "l2v", l2f1, l2f2);
        Locator locator2 = new Locator();
        locator2.getFields().add(l2f);
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
