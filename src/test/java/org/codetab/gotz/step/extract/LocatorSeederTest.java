package org.codetab.gotz.step.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.di.DInjector;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * <p>
 * LocatorSeeder Tests.
 *
 * @author Maithilish
 *
 */

public class LocatorSeederTest {

    @Mock
    private StepService stepService;
    @Mock
    private LocatorHelper locatorHelper;
    @Mock
    private LocatorFieldsHelper locatorFieldsHelper;
    @Spy
    private FieldsHelper fieldsHelper;
    @Spy
    private DInjector dInjector;

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
    public void testSetInput() throws IllegalAccessException, FieldsException {
        Locator locator = new Locator();
        locator.setName("x");
        locator.setGroup("gx");
        Fields fields = TestUtil.createFields("f1", "v1");
        locator.setFields(fields);

        // when
        locatorSeeder.setInput(locator);

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).contains(locator);
        assertThat(actual.get(0).getFields().getNodes().size()).isEqualTo(1);
    }

    @Test
    public void testSetInputOtherThanLocator() throws IllegalAccessException {

        // when
        locatorSeeder.setInput("some object");

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual.size()).isEqualTo(0);
    }

    @Test
    public void testInitialize() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();

        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);

        // when
        locatorSeeder.initialize();

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual).isEqualTo(locators);
    }

    @Test
    public void testInitializeAfterSetInput() throws IllegalAccessException {
        Locator locator = new Locator();
        locator.setName("x");
        locator.setGroup("gx");

        locatorSeeder.setInput(locator);

        // when
        locatorSeeder.initialize();

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).contains(locator);

        verifyZeroInteractions(locatorHelper);
    }

    @Test
    public void testInitializeForkedLocators() throws IllegalAccessException {
        List<Locator> locators = createTestObjects();

        List<Locator> forkedLocators = createTestObjects();
        Locator extraLocator = new Locator();
        extraLocator.setName("x");
        extraLocator.setGroup("gx");
        forkedLocators.add(extraLocator);

        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        given(locatorHelper.forkLocators(locators)).willReturn(forkedLocators);

        // when
        locatorSeeder.initialize();

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual).isEqualTo(forkedLocators);
    }

    @Test
    public void testInitializeForkedLocatorsEmptyList()
            throws IllegalAccessException {
        List<Locator> locators = createTestObjects();

        List<Locator> forkedLocators = new ArrayList<>();

        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        given(locatorHelper.forkLocators(locators)).willReturn(forkedLocators);

        // when
        locatorSeeder.initialize();

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual).isEqualTo(locators);
    }

    @Test
    public void testProcessSetGroupFields() throws FieldsException {
        List<Locator> locators = createTestObjects();
        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        locatorSeeder.initialize();

        Fields groupOneFields = fieldsHelper.createFields();
        fieldsHelper.addElement("x", "xv", groupOneFields);
        Fields groupTwoFields = fieldsHelper.createFields();
        fieldsHelper.addElement("y", "yv", groupOneFields);

        given(locatorFieldsHelper.getFields(Locator.class.getName(), "g1"))
                .willReturn(groupOneFields);
        given(locatorFieldsHelper.getFields(Locator.class.getName(), "g2"))
                .willReturn(groupTwoFields);

        // when
        locatorSeeder.process();

        assertThat(locators.get(0).getFields()).isEqualTo(groupOneFields);
        assertThat(locators.get(1).getFields()).isEqualTo(groupOneFields);
        assertThat(locators.get(2).getFields()).isEqualTo(groupTwoFields);
    }

    @Test
    public void testHandover() throws FieldsException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        Fields fields1 = fieldsHelper.createFields();
        fieldsHelper.addElement("x", "xv", fields1);
        Fields fields2 = fieldsHelper.createFields();
        fieldsHelper.addElement("y", "yv", fields2);
        Fields fields3 = fieldsHelper.createFields();
        fieldsHelper.addElement("z", "zv", fields3);

        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        locator1.setFields(fields1);
        Locator locator2 = locators.get(1);
        locator2.setFields(fields2);
        Locator locator3 = locators.get(2);
        locator3.setFields(fields3);

        Labels labels1 = new Labels(locator1.getName(), locator1.getGroup());
        Labels labels2 = new Labels(locator2.getName(), locator2.getGroup());
        Labels labels3 = new Labels(locator3.getName(), locator3.getGroup());

        LocatorSeeder locatorSeeder1 = dInjector.instance(LocatorSeeder.class);
        LocatorSeeder locatorSeeder2 = dInjector.instance(LocatorSeeder.class);
        LocatorSeeder locatorSeeder3 = dInjector.instance(LocatorSeeder.class);

        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        when(stepService.getStep(LocatorSeeder.class.getName()))
                .thenReturn(locatorSeeder1, locatorSeeder2, locatorSeeder3);
        given(locatorHelper.createLabels(locator1)).willReturn(labels1);
        given(locatorHelper.createLabels(locator2)).willReturn(labels2);
        given(locatorHelper.createLabels(locator3)).willReturn(labels3);

        locatorSeeder.initialize();

        locatorSeeder.handover();

        InOrder inOrder = inOrder(stepService);
        inOrder.verify(stepService).pushTask(locatorSeeder1, locator1, labels1,
                locator1.getFields());
        inOrder.verify(stepService).pushTask(locatorSeeder2, locator2, labels2,
                locator2.getFields());
        inOrder.verify(stepService).pushTask(locatorSeeder3, locator3, labels3,
                locator3.getFields());
    }

    private List<Locator> createTestObjects() {
        Locator locator1 = new Locator();
        locator1.setName("n1");
        locator1.setGroup("g1");

        Locator locator2 = new Locator();
        locator2.setName("n2");
        locator2.setGroup("g1");

        Locator locator3 = new Locator();
        locator3.setName("n3");
        locator3.setGroup("g2");

        List<Locator> locators = new ArrayList<>();
        locators.add(locator1);
        locators.add(locator2);
        locators.add(locator3);

        return locators;
    }

}
