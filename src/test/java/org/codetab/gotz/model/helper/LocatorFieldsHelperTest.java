package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.testutil.TestJaxbHelper;
import org.codetab.gotz.util.FieldsUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Maithilish
 *
 */
public class LocatorFieldsHelperTest {

    @Mock
    private BeanService beanService;

    @InjectMocks
    private LocatorFieldsHelper fieldsHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit() throws JAXBException, IOException,
            IllegalAccessException, FieldNotFoundException {
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(
                "/testdefs/locatorfieldshelper/class-step-fields.xml",
                FieldsBase.class);

        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);

        // when
        fieldsHelper.init();

        // then
        // classFields
        @SuppressWarnings("unchecked")
        List<FieldsBase> classFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "classFields", true);
        List<FieldsBase> expected = FieldsUtil.filterByValue(fields, "class",
                Locator.class.getName());
        assertThat(classFields.size()).isEqualTo(1);
        assertThat(classFields).isEqualTo(expected);

        // stepFields
        @SuppressWarnings("unchecked")
        List<FieldsBase> stepFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "stepFields", true);
        List<FieldsBase> clzFields = FieldsUtil.filterByValue(fields, "class",
                Locator.class.getName());
        List<FieldsBase> stepsGroup =
                FieldsUtil.filterByGroup(clzFields, "steps");
        expected = FieldsUtil.filterByName(stepsGroup, "step");

        assertThat(stepFields.size()).isEqualTo(2);
        assertThat(stepFields).isEqualTo(expected);
    }

    @Test
    public void testInitMultipleInvoke() throws JAXBException, IOException,
            IllegalAccessException, FieldNotFoundException {
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(
                "/testdefs/locatorfieldshelper/class-step-fields.xml",
                FieldsBase.class);

        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);

        // when
        fieldsHelper.init();
        fieldsHelper.init();

        // then
        // classFields
        @SuppressWarnings("unchecked")
        List<FieldsBase> classFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "classFields", true);
        List<FieldsBase> expected = FieldsUtil.filterByValue(fields, "class",
                Locator.class.getName());
        assertThat(classFields.size()).isEqualTo(1);
        assertThat(classFields).isEqualTo(expected);

        // stepFields
        @SuppressWarnings("unchecked")
        List<FieldsBase> stepFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "stepFields", true);
        List<FieldsBase> clzFields = FieldsUtil.filterByValue(fields, "class",
                Locator.class.getName());
        List<FieldsBase> stepsGroup =
                FieldsUtil.filterByGroup(clzFields, "steps");
        expected = FieldsUtil.filterByName(stepsGroup, "step");

        assertThat(stepFields.size()).isEqualTo(2);
        assertThat(stepFields).isEqualTo(expected);
    }

    @Test
    public void testInitFieldsNotDefined() throws IllegalAccessException {
        List<FieldsBase> fields = new ArrayList<>();
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);

        // when
        fieldsHelper.init();

        @SuppressWarnings("unchecked")
        List<FieldsBase> stepFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "stepFields", true);
        @SuppressWarnings("unchecked")
        List<FieldsBase> classFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "classFields", true);

        assertThat(stepFields.size()).isEqualTo(0);
        assertThat(classFields.size()).isEqualTo(0);
    }

    @Test
    public void testInitIllegalState() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(fieldsHelper, "beanService", null, true);

        try {
            fieldsHelper.init();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("beanService is null");
        }
    }

    @Test
    public void testGetStepFields()
            throws JAXBException, IOException, IllegalAccessException {
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(
                "/testdefs/locatorfieldshelper/class-step-fields.xml",
                FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        // when
        List<FieldsBase> actual = fieldsHelper.getStepFields();

        @SuppressWarnings("unchecked")
        List<FieldsBase> stepFields = (List<FieldsBase>) FieldUtils
                .readDeclaredField(fieldsHelper, "stepFields", true);

        assertThat(actual).isEqualTo(stepFields);
        assertThat(actual).isNotSameAs(stepFields);
    }

    @Test
    public void testGetStepFieldsIllegalState() {
        try {
            fieldsHelper.getStepFields();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("LocatorFieldsHelper is not initialized");
        }
    }

    @Test
    public void testGetLocatorGroupFieldsInitialized()
            throws JAXBException, IOException {
        String fieldsFile =
                "/testdefs/locatorfieldshelper/datadef-step-fields.xml";
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        // when
        List<FieldsBase> actual = fieldsHelper.getLocatorGroupFields("g1");

        assertThat(actual.size()).isEqualTo(1);
    }

    @Test
    public void testGetLocatorGroupFieldsInvalidGroup()
            throws JAXBException, IOException {
        String fieldsFile =
                "/testdefs/locatorfieldshelper/datadef-step-fields.xml";
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        // when
        List<FieldsBase> actual = fieldsHelper.getLocatorGroupFields("x");

        assertThat(actual.size()).isEqualTo(0);
    }

    @Test
    public void testGetLocatorGroupFields()
            throws JAXBException, IOException, FieldNotFoundException {
        String fieldsFile =
                "/testdefs/locatorfieldshelper/datadef-step-fields.xml";
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        List<FieldsBase> actual = fieldsHelper.getLocatorGroupFields("g1");

        List<FieldsBase> expected = getExpectedGroupFields(fieldsFile, "g1");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void testGetLocatorGroupFieldsTwice()
            throws JAXBException, IOException, FieldNotFoundException {
        String fieldsFile =
                "/testdefs/locatorfieldshelper/datadef-step-fields.xml";
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        // when
        List<FieldsBase> actual1 = fieldsHelper.getLocatorGroupFields("g1");
        List<FieldsBase> actual2 = fieldsHelper.getLocatorGroupFields("g1");

        List<FieldsBase> expected = getExpectedGroupFields(fieldsFile, "g1");

        assertThat(actual1.size()).isEqualTo(expected.size());
        assertThat(actual1).containsAll(expected);
        assertThat(actual2.size()).isEqualTo(expected.size());
        assertThat(actual2).containsAll(expected);

        assertThat(actual1).isEqualTo(actual2);
        assertThat(actual1).isNotSameAs(actual2); // deep clone
    }

    @Test
    public void testGetLocatorGroupFieldsNoStepsAtDataDefLevel()
            throws JAXBException, IOException, FieldNotFoundException {
        String fieldsFile =
                "/testdefs/locatorfieldshelper/datadef-step-fields.xml";
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        // when
        List<FieldsBase> actual = fieldsHelper.getLocatorGroupFields("g2");
        List<FieldsBase> expected = getExpectedGroupFields(fieldsFile, "g2");

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void testGetLocatorGroupFieldsNoDataDefGroup()
            throws JAXBException, IOException, FieldNotFoundException {
        String fieldsFile =
                "/testdefs/locatorfieldshelper/datadef-step-fields.xml";
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fields);
        fieldsHelper.init();

        // when
        List<FieldsBase> actual = fieldsHelper.getLocatorGroupFields("g3");

        assertThat(actual.size()).isEqualTo(0);
    }

    @Test
    public void testGetLocatorGroupFieldsIllegalState()
            throws IllegalAccessException {
        FieldUtils.writeDeclaredField(fieldsHelper, "stepFields", null, true);
        FieldUtils.writeDeclaredField(fieldsHelper, "classFields",
                new ArrayList<>(), true);

        try {
            fieldsHelper.getLocatorGroupFields("x");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("LocatorFieldsHelper is not initialized");
        }

        FieldUtils.writeDeclaredField(fieldsHelper, "stepFields",
                new ArrayList<>(), true);
        FieldUtils.writeDeclaredField(fieldsHelper, "classFields", null, true);

        try {
            fieldsHelper.getLocatorGroupFields("x");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("LocatorFieldsHelper is not initialized");
        }
    }

    @Test
    public void testGetLocatorGroupFieldsNullParams() {
        try {
            fieldsHelper.getLocatorGroupFields(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("group must not be null");
        }
    }

    @Test
    public void testAddLabel() {
        Locator locator = new Locator();
        locator.setName("n");
        locator.setGroup("g");
        locator.setUrl("g");

        // when
        fieldsHelper.addLabel(locator);

        // then
        assertThat(locator.getFields().size()).isEqualTo(1);

        FieldsBase actual = locator.getFields().get(0);

        assertThat(actual.getName()).isEqualTo("label");
        assertThat(actual.getValue()).isEqualTo("n:g");
    }

    @Test
    public void testAddLabelNullParams() {
        try {
            fieldsHelper.addLabel(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("locator must not be null");
        }
    }

    private List<FieldsBase> getExpectedGroupFields(final String fieldsFile,
            final String groupName)
            throws FieldNotFoundException, JAXBException, IOException {
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(fieldsFile, FieldsBase.class);
        List<FieldsBase> classFields = FieldsUtil.filterByValue(fields, "class",
                Locator.class.getName());
        List<FieldsBase> stepsGroup =
                FieldsUtil.filterByGroup(classFields, "steps");
        List<FieldsBase> stepFields =
                FieldsUtil.filterByName(stepsGroup, "step");

        List<FieldsBase> groupFields =
                FieldsUtil.filterByGroup(classFields, groupName);
        List<Fields> dataDefGroup =
                FieldsUtil.filterByGroupAsFields(groupFields, "datadef");
        for (Fields dataDefFields : dataDefGroup) {
            for (FieldsBase step : stepFields) {
                // if step is not defined for datadef, then add it
                if (!FieldsUtil.contains(dataDefFields, step.getName(),
                        step.getValue())) {
                    dataDefFields.getFields().add(step);
                }
            }
        }
        return groupFields;
    }
}
