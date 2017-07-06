package org.codetab.gotz.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import org.codetab.gotz.helper.DocumentHelper;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.persistence.DocumentPersistence;
import org.codetab.gotz.persistence.LocatorPersistence;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

public class LoaderTest {

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

    @InjectMocks
    private URLLoader loader;
    private String fileUrl;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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

        boolean actual = loader.initialize();
        Marker marker = (Marker) FieldUtils.readField(loader, "marker", true);

        assertThat(actual).isTrue();
        assertThat(marker.getName()).isEqualTo("LOG_L1_G1");
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

    @Test
    public void testLoadSavedLocator() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator newLocator = locators.get(0);
        Locator savedLocator = locators.get(1);

        newLocator.setUrl(fileUrl);

        loader.setInput(newLocator);
        given(locatorPersistence.loadLocator("l1", "g1"))
                .willReturn(savedLocator);

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
    public void testLoadNewLocator() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator newLocator = locators.get(0);
        Locator locator2 = locators.get(1);

        newLocator.setUrl(fileUrl);

        loader.setInput(newLocator);
        given(locatorPersistence.loadLocator("l1", "g1")).willReturn(null);

        boolean actual = loader.load();
        Locator locator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(loader.getStepState()).isEqualTo(StepState.LOAD);
        assertThat(actual).isTrue();

        assertThat(locator).isSameAs(newLocator);
        assertThat(newLocator.getUrl()).isEqualTo(fileUrl);
        assertThat(locator2.getUrl()).isEqualTo("url2");

        assertThat(newLocator.getFields().size()).isEqualTo(1);
        assertThat(locator2.getFields().size()).isEqualTo(1);
        assertThat(newLocator.getFields())
                .doesNotContainAnyElementsOf(locator2.getFields());
    }

    @Test
    public void testProcessLocatorIdSet() throws IllegalAccessException {
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

        boolean actual = loader.process();

        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        verify(documentHelper).getActiveDocumentId(documents);
        verify(documentPersistence).loadDocument(documentId);
    }

    @Test
    public void testProcessLocatorIdNotSet() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        locator.setUrl(fileUrl);

        List<Document> documents = locator.getDocuments();

        given(dInjector.instance(Document.class)).willReturn(new Document());

        FieldUtils.writeField(loader, "locator", locator, true);

        boolean actual = loader.process();

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

        boolean actual = loader.process();

        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        verify(documentPersistence).loadDocument(documentId);
        assertThat(loader.isConsistent()).isTrue();
    }

    @Test
    public void testProcessNoActiveDocument()
            throws IllegalAccessException, IOException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);
        locator.setUrl(fileUrl);

        List<Document> documents = locator.getDocuments();
        Document document1 = documents.get(0);
        Document document2 = new Document();

        FieldUtils.writeField(loader, "locator", locator, true);

        Date fromDate = new Date();
        Date toDate = DateUtils.addMonths(fromDate, 1);
        Object docObject = loader.fetchDocument(fileUrl);

        given(dInjector.instance(Document.class)).willReturn(document2);
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(eq(fromDate), eq(locator.getFields()),
                any(String.class))).willReturn(toDate);

        boolean actual = loader.process();

        assertThat(loader.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(actual).isTrue();

        assertThat(document2.getName()).isEqualTo(locator.getName());
        assertThat(document2.getFromDate()).isEqualTo(fromDate);
        assertThat(document2.getToDate()).isEqualTo(toDate);
        assertThat(document2.getUrl()).isEqualTo(fileUrl);
        assertThat(document2.getDocumentObject()).isEqualTo(docObject);

        assertThat(locator.getDocuments().size()).isEqualTo(2);
        assertThat(locator.getDocuments()).contains(document1);
        assertThat(locator.getDocuments()).contains(document2);

        verifyZeroInteractions(documentPersistence);
        assertThat(loader.isConsistent()).isTrue();
    }

    @Test
    public void testProcessExpectException() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        FieldUtils.writeField(loader, "locator", locator, true);

        try {
            loader.process();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(IOException.class));
        }

        exceptionRule.expect(StepRunException.class);
        loader.process();
    }

    @Test
    public void testStorePersistSetFalse() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        Field field = TestUtil.createField("document", "false");
        Fields fields = TestUtil.createFields("group", "persist", field);
        locator.getFields().add(fields);

        FieldUtils.writeField(loader, "locator", locator, true);

        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        verifyZeroInteractions(documentPersistence);
        verifyZeroInteractions(locatorPersistence);
    }

    @Test
    public void testStore() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        Locator locator2 = locators.get(1);
        Document document1 = locator1.getDocuments().get(0);
        Document document2 = new Document();

        FieldUtils.writeField(loader, "locator", locator1, true);
        FieldUtils.writeField(loader, "document", document1, true);

        given(locatorPersistence.loadLocator(locator1.getId()))
                .willReturn(locator2);
        given(documentPersistence.loadDocument(document1.getId()))
                .willReturn(document2);

        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        verify(locatorPersistence).storeLocator(locator1);
        assertThat(locator2.getFields()).containsAll(locator1.getFields());

        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualLocator).isSameAs(locator2);
        assertThat(actualDocument).isSameAs(document2);
    }

    @Test
    public void testStorePersistSetTrue() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        Locator locator2 = locators.get(1);
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

        boolean actual = loader.store();

        assertThat(loader.getStepState()).isEqualTo(StepState.STORE);
        assertThat(actual).isTrue();

        verify(locatorPersistence).storeLocator(locator1);
        assertThat(locator2.getFields()).containsAll(locator1.getFields());

        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        Document actualDocument =
                (Document) FieldUtils.readField(loader, "document", true);

        assertThat(actualLocator).isSameAs(locator2);
        assertThat(actualDocument).isSameAs(document2);
    }

    @Test
    public void testStoreExpectException() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        FieldUtils.writeField(loader, "locator", locator, true);

        given(locatorPersistence.loadLocator(locator.getId()))
                .willThrow(RuntimeException.class);

        try {
            loader.store();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(RuntimeException.class));
        }

        exceptionRule.expect(StepRunException.class);
        loader.store();
    }

    @Test
    public void testHandoverNoStepFields() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        FieldUtils.writeField(loader, "locator", locator, true);

        try {
            loader.handover();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldNotFoundException.class));
        }

        exceptionRule.expect(StepRunException.class);
        loader.handover();
    }

    @Test
    public void testHandoverNoDataDefFields() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        Field step = TestUtil.createField("step1", "x");
        Fields steps = TestUtil.createFields("group", "steps", step);
        locator.getFields().add(steps);

        FieldUtils.writeField(loader, "locator", locator, true);

        try {
            loader.handover();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldNotFoundException.class));
        }

        exceptionRule.expect(StepRunException.class);
        loader.handover();
    }

    @Test
    public void testHandoverDataDefIsFieldInstance()
            throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        Field step = TestUtil.createField("step1", "x");
        Fields steps = TestUtil.createFields("group", "steps", step);
        locator.getFields().add(steps);

        Field dataDef1 = TestUtil.createField("datadef", "d1");
        Field dataDef2 = TestUtil.createField("datadef", "d2");
        Fields dataDefs =
                TestUtil.createFields("group", "datadef", dataDef1, dataDef2);
        locator.getFields().add(dataDefs);

        FieldUtils.writeField(loader, "locator", locator, true);

        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        verifyZeroInteractions(stepService);
    }

    @Test
    public void testHandoverDocumentNotLoaded() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();
        Locator locator = locators.get(0);

        Field step = TestUtil.createField("step1", "x");
        Fields steps = TestUtil.createFields("group", "steps", step);
        locator.getFields().add(steps);

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
        Fields dataDefs =
                TestUtil.createFields("group", "datadef", dataDef1, dataDef2);
        locator.getFields().add(dataDefs);

        FieldUtils.writeField(loader, "locator", locator, true);
        FieldUtils.writeField(loader, "document", document, true);

        // expected fields
        Fields fd1 = TestUtil.createFields("datadef", "d1", f1);
        Fields fd2 = TestUtil.createFields("datadef", "d2", f2);
        Fields dataDefFields =
                TestUtil.createFields("group", "datadef", fd1, fd2);

        List<FieldsBase> expectedFields = new ArrayList<>();
        expectedFields.add(dataDefFields);
        expectedFields
                .add(TestUtil.createField("locatorName", locator.getName()));
        expectedFields
                .add(TestUtil.createField("locatorGroup", locator.getGroup()));
        expectedFields
                .add(TestUtil.createField("locatorUrl", locator.getUrl()));

        boolean actual = loader.handover();

        assertThat(loader.getStepState()).isEqualTo(StepState.HANDOVER);
        assertThat(actual).isTrue();

        verify(stepService).pushTask(loader, document, expectedFields);
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
