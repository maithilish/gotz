package org.codetab.gotz.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.FieldNotFoundException;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Locators;
import org.codetab.gotz.shared.BeanService;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.testutil.TestJaxbHelper;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.util.FieldsUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LocatorSeederTest {

    @Mock
    private StepService stepService;
    @Mock
    private BeanService beanService;

    @InjectMocks
    private LocatorSeeder locatorSeeder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstance() {
        assertThat(locatorSeeder.isConsistent()).isFalse();
        assertThat(locatorSeeder.getStepType()).isNull();
        assertThat(locatorSeeder.instance().getStepType()).isEqualTo("seeder");
    }

    @Test
    public void testLoad() {
        assertThat(locatorSeeder.load()).isFalse();
    }

    @Test
    public void testProcessTrikleGroup() {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorSeeder.process();

        // then
        assertThat(locator1.getGroup()).isEqualTo("g1");
        assertThat(locator2.getGroup()).isEqualTo("g1");
        assertThat(locator3.getGroup()).isEqualTo("g1");
    }

    @Test
    public void testProcessTrikleGroupLocatorGroupNotNull() {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        locator1.setGroup("gx");

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorSeeder.process();

        // then
        assertThat(locator1.getGroup()).isEqualTo("gx");
        assertThat(locator2.getGroup()).isEqualTo("g1");
        assertThat(locator3.getGroup()).isEqualTo("g1");
    }

    @Test
    public void testProcessTrikleGroupChildLocatorsGroupNotNull() {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        // set group of child locators
        Locators locs = locators.get(0).getLocators().get(0);
        locs.setGroup("gx");

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorSeeder.process();

        // then
        assertThat(locator1.getGroup()).isEqualTo("g1");
        assertThat(locator2.getGroup()).isEqualTo("g1");
        assertThat(locator3.getGroup()).isEqualTo("gx");
    }

    @Test
    public void testProcessExtractLocators() throws IllegalAccessException {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorSeeder.process();

        // then
        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils.readDeclaredField(locatorSeeder,
                "locators", true);
        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual).contains(locator1);
        assertThat(actual).contains(locator2);
        assertThat(actual).contains(locator3);
    }

    @Test
    public void testProcessAddLabel() throws IllegalAccessException {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        // when
        locatorSeeder.process();

        // then
        Field field = new Field();
        field.setName("label");

        field.setValue("n1:g1");
        assertThat(locator1.getFields()).contains(field);
        field.setValue("n2:g1");
        assertThat(locator2.getFields()).contains(field);
        field.setValue("n3:g1");
        assertThat(locator3.getFields()).contains(field);
    }

    @Test
    public void testProcessInitFieldsSetSteps() throws IllegalAccessException,
    JAXBException, IOException, FieldNotFoundException {
        List<Locators> locators = createTestObjects2();
        List<FieldsBase> fieldsList = createTestFields();
        List<FieldsBase> stepFields = FieldsUtil.filterByName(fieldsList, "steps",
                "step");

        given(beanService.getBeans(Locators.class)).willReturn(locators);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fieldsList);

        // when
        locatorSeeder.process();

        // then
        assertThat(locatorSeeder.getFields()).isEqualTo(stepFields);
    }

    @Test
    public void testProcessInitFieldsMergeFields() throws IllegalAccessException,
    JAXBException, IOException, FieldNotFoundException {
        // given
        List<Locators> locators = createTestObjects2();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(1).getLocator().get(0);

        List<FieldsBase> fieldsList = createTestFields();
        List<FieldsBase> classFields = FieldsUtil.filterByValue(fieldsList, "class",
                Locator.class.getName());
        List<FieldsBase> stepFields = FieldsUtil.filterByName(fieldsList, "steps",
                "step");

        given(beanService.getBeans(Locators.class)).willReturn(locators);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fieldsList);

        // when
        locatorSeeder.process();

        List<Fields> dataDefGroup = FieldsUtil.filterByGroupAsFields(classFields,
                "datadef");
        for (Fields dataDefFields : dataDefGroup) {
            for (FieldsBase step : stepFields) {
                if (!FieldsUtil.contains(dataDefFields, step.getName(),
                        step.getValue())) {
                    dataDefFields.getFields().add(step);
                }
            }
        }

        // then
        List<FieldsBase> locatorGroupFields = new ArrayList<>();
        Field label = TestUtil.createField("label",
                locator1.getName() + ":" + locator1.getGroup());
        locatorGroupFields.add(label);
        locatorGroupFields
        .addAll(FieldsUtil.filterByGroup(classFields, locator1.getGroup()));
        assertThat(locator1.getFields()).isEqualTo(locatorGroupFields);


        locatorGroupFields = new ArrayList<>();
        label = TestUtil.createField("label",
                locator2.getName() + ":" + locator2.getGroup());
        locatorGroupFields.add(label);
        locatorGroupFields
        .addAll(FieldsUtil.filterByGroup(classFields, locator2.getGroup()));
        assertThat(locator2.getFields()).isEqualTo(locatorGroupFields);
    }

    @Test
    public void testProcessInitFieldsMergeFieldsWhenClassFieldIsNull()
            throws IllegalAccessException, JAXBException, IOException {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        List<FieldsBase> fieldsList = createTestFields();
        Fields classFields = (Fields) fieldsList.get(0);
        classFields.setName("xyz");
        // FieldsBase stepFields = classFields.getFields().get(0);
        FieldsBase groupFields = classFields.getFields().get(1);

        given(beanService.getBeans(Locators.class)).willReturn(locators);
        given(beanService.getBeans(FieldsBase.class)).willReturn(fieldsList);

        // when
        locatorSeeder.process();

        // then
        assertThat(locator1.getFields()).doesNotContain(groupFields);
        assertThat(locator2.getFields()).doesNotContain(groupFields);
        assertThat(locator3.getFields()).doesNotContain(groupFields);
        assertThat(locator1.getFields()).doesNotContain(groupFields);
        assertThat(locator2.getFields()).doesNotContain(groupFields);
        assertThat(locator3.getFields()).doesNotContain(groupFields);
    }

    @Test
    public void testHandover() throws IllegalAccessException {
        // given
        List<Locators> locators = createTestObjects();
        Locator locator1 = locators.get(0).getLocator().get(0);
        Locator locator2 = locators.get(0).getLocator().get(1);
        Locator locator3 = locators.get(0).getLocators().get(0).getLocator().get(0);

        given(beanService.getBeans(Locators.class)).willReturn(locators);

        locatorSeeder.process(); // set locator list

        // when
        boolean actual = locatorSeeder.handover();

        // then
        assertThat(actual).isTrue();

        InOrder inOrder = inOrder(stepService);
        inOrder.verify(stepService).pushTask(locatorSeeder, locator3,
                locator3.getFields());
        inOrder.verify(stepService).pushTask(locatorSeeder, locator1,
                locator1.getFields());
        inOrder.verify(stepService).pushTask(locatorSeeder, locator2,
                locator2.getFields());
    }

    @Test
    public void testStore() {
        assertThat(locatorSeeder.store()).isFalse();
    }

    @Test
    public void testSetInput() throws IllegalAccessException {
        locatorSeeder.setInput("x");
        // for coverage
    }

    private List<Locators> createTestObjects() {
        Field field = new Field();
        field.setName("f");

        Locator locator1 = new Locator();
        locator1.setName("n1");
        locator1.getFields().add(field);

        Locator locator2 = new Locator();
        locator2.setName("n2");
        locator2.getFields().add(field);

        Locators locators1 = new Locators();
        locators1.setGroup("g1");
        locators1.getLocator().add(locator1);
        locators1.getLocator().add(locator2);

        Locator locator3 = new Locator();
        locator3.setName("n3");
        locator3.getFields().add(field);

        Locators locators2 = new Locators();
        locators2.getLocator().add(locator3);

        locators1.getLocators().add(locators2);

        List<Locators> locators = new ArrayList<>();
        locators.add(locators1);

        return locators;
    }

    private List<FieldsBase> createTestFields() throws JAXBException, IOException {
        TestJaxbHelper jh = new TestJaxbHelper();
        List<FieldsBase> fields = jh.unmarshall(
                "/testdefs/locatorseeder/locatorfields1.xml", FieldsBase.class);
        return fields;
    }

    private List<Locators> createTestObjects2() throws JAXBException, IOException {
        TestJaxbHelper jh = new TestJaxbHelper();
        List<Locators> locators = jh.unmarshall("/testdefs/locatorseeder/locator1.xml",
                Locators.class);
        return locators;
    }
}
