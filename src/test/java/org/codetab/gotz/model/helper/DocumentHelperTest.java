package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.ConfigNotFoundException;
import org.codetab.gotz.model.Document;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.util.CompressionUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DocumentHelper Tests.
 *
 * @author Maithilish
 *
 */
public class DocumentHelperTest {

    @Mock
    private ConfigService configService;
    @Mock
    private DInjector dInjector;

    @InjectMocks
    private DocumentHelper documentHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetActiveDocumentId() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};

        Document doc1 = new Document();
        doc1.setId(1L);
        Date toDate =
                DateUtils.parseDate("01-07-2017 09:59:59.999", parsePatterns);
        doc1.setToDate(toDate);

        Document doc2 = new Document();
        doc2.setId(2L);
        toDate = DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        doc2.setToDate(toDate);

        List<Document> documents = new ArrayList<>();
        documents.add(doc1);
        documents.add(doc2);

        Date runDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);

        given(configService.getRunDateTime()).willReturn(runDate);

        Long actual = documentHelper.getActiveDocumentId(documents);

        assertThat(actual).isEqualTo(2L);

        runDate = DateUtils.parseDate("01-07-2017 09:59:59.999", parsePatterns);
        given(configService.getRunDateTime()).willReturn(runDate);

        actual = documentHelper.getActiveDocumentId(documents);

        assertThat(actual).isEqualTo(2L);

        runDate = DateUtils.parseDate("01-07-2017 10:00:00.001", parsePatterns);
        given(configService.getRunDateTime()).willReturn(runDate);

        actual = documentHelper.getActiveDocumentId(documents);

        assertThat(actual).isNull();
    }

    @Test
    public void testGetActiveDocumentIdDocumentsNull() throws ParseException {
        List<Document> documents = null;
        Long actual = documentHelper.getActiveDocumentId(documents);
        assertThat(actual).isNull();
    }

    @Test
    public void testGetActiveDocumentIdDocumentsEmpty() throws ParseException {
        List<Document> documents = new ArrayList<>();
        Long actual = documentHelper.getActiveDocumentId(documents);
        assertThat(actual).isNull();
    }

    @Test
    public void testGetToDateNoLiveField() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        List<FieldsBase> fields = new ArrayList<>();

        // when
        Date actual = documentHelper.getToDate(fromDate, fields);

        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateWithLiveField() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);

        FieldsBase live = TestUtil.createField("live", "P2D");
        List<FieldsBase> fields = TestUtil.asList(live);
        fields.add(TestUtil.createField("label", "x:y"));

        Date expected = DateUtils.addDays(fromDate, 2);

        // when
        Date actual = documentHelper.getToDate(fromDate, fields);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetToDateWithBlankOrZeroLiveField() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        List<FieldsBase> fields =
                TestUtil.asList(TestUtil.createField("live", ""));

        // when
        Date actual = documentHelper.getToDate(fromDate, fields);
        assertThat(actual).isEqualTo(fromDate);

        fields = TestUtil.asList(TestUtil.createField("live", "0"));
        actual = documentHelper.getToDate(fromDate, fields);
        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateWithDateString()
            throws ParseException, ConfigNotFoundException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate = new Date();
        String toDateStr = "01-08-2017 11:00:00.000";
        List<FieldsBase> fields =
                TestUtil.asList(TestUtil.createField("live", toDateStr));

        given(configService.getConfigArray("gotz.dateParsePattern"))
                .willReturn(parsePatterns);
        Date expected = DateUtils.parseDate(toDateStr, parsePatterns);

        // when
        Date actual = documentHelper.getToDate(fromDate, fields);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetToDateWithInvalidDateString()
            throws ParseException, ConfigNotFoundException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate = new Date();
        String toDateStr = "01-xx-2017 11:00:00.000";
        List<FieldsBase> fields =
                TestUtil.asList(TestUtil.createField("live", toDateStr));

        given(configService.getConfigArray("gotz.dateParsePattern"))
                .willReturn(parsePatterns);

        // when
        Date actual = documentHelper.getToDate(fromDate, fields);
        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateParsePatternNotFound()
            throws ParseException, ConfigNotFoundException {
        Date fromDate = new Date();
        String toDateStr = "01-xx-2017 11:00:00.000";
        List<FieldsBase> fields =
                TestUtil.asList(TestUtil.createField("live", toDateStr));

        given(configService.getConfigArray("gotz.dateParsePattern"))
                .willThrow(ConfigNotFoundException.class);

        // when
        Date actual = documentHelper.getToDate(fromDate, fields);
        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateNullParams() {
        try {
            documentHelper.getToDate(null, new ArrayList<>());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fromDate must not be null");
        }

        try {
            documentHelper.getToDate(new Date(), null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fields must not be null");
        }
    }

    @Test
    public void testSetDocumentObjectCompression() throws IOException {
        Document document = new Document();
        byte[] documentObject = String.valueOf("some string").getBytes();

        // when
        boolean actual =
                documentHelper.setDocumentObject(document, documentObject);

        byte[] expected =
                CompressionUtil.compressByteArray(documentObject, 1024);

        assertThat(actual).isTrue();
        assertThat(document.getDocumentObject()).isEqualTo(expected);
    }

    @Test
    public void testSetDocumentObjectNullParams() throws IOException {
        try {
            documentHelper.setDocumentObject(null, new String().getBytes());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("document must not be null");
        }

        try {
            documentHelper.setDocumentObject(new Document(), null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("documentObject must not be null");
        }
    }

    @Test
    public void testGetDocumentObjectNullParams()
            throws DataFormatException, IOException {
        try {
            documentHelper.getDocumentObject(null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("document must not be null");
        }

        try {
            // document without documentObject
            documentHelper.getDocumentObject(new Document());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("documentObject must not be null");
        }
    }

    @Test
    public void testGetDocumentObject()
            throws IOException, DataFormatException {
        Document document = new Document();
        byte[] documentObject = String.valueOf("some string").getBytes();
        documentHelper.setDocumentObject(document, documentObject);

        // when
        byte[] actual = documentHelper.getDocumentObject(document);

        assertThat(actual).isEqualTo(documentObject);
    }

    @Test
    public void testCreateDocumentNullParams() {
        try {
            documentHelper.createDocument(null, "y", new Date(), new Date());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            documentHelper.createDocument("x", null, new Date(), new Date());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("url must not be null");
        }

        try {
            documentHelper.createDocument("x", "y", null, new Date());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fromDate must not be null");
        }

        try {
            documentHelper.createDocument("x", "y", new Date(), null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("toDate must not be null");
        }
    }

    @Test
    public void testCreateDocument() {
        Document document = new Document();
        Date fromDate = new Date();
        Date toDate = new Date();

        given(dInjector.instance(Document.class)).willReturn(document);

        Document actual =
                documentHelper.createDocument("x", "y", fromDate, toDate);

        assertThat(actual).isSameAs(document);
        assertThat(actual.getName()).isEqualTo("x");
        assertThat(actual.getUrl()).isEqualTo("y");
        assertThat(actual.getFromDate()).isEqualTo(fromDate);
        assertThat(actual.getToDate()).isEqualTo(toDate);
    }
}
