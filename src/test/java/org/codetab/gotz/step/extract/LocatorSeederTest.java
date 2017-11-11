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
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.model.helper.LocatorXFieldHelper;
import org.codetab.gotz.model.helper.XFieldHelper;
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
    private LocatorFieldsHelper fieldsHelper;
    @Mock
    private LocatorXFieldHelper locatorXFieldHelper;
    @Spy
    private XFieldHelper xFieldHelper;
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
    public void testSetInput() throws IllegalAccessException {
        Locator locator = new Locator();
        locator.setName("x");
        locator.setGroup("gx");
        FieldsBase field = TestUtil.createField("f1", "v1");
        locator.getFields().add(field);

        // when
        locatorSeeder.setInput(locator);

        @SuppressWarnings("unchecked")
        List<Locator> actual = (List<Locator>) FieldUtils
                .readDeclaredField(locatorSeeder, "locatorList", true);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).contains(locator);
        assertThat(actual.get(0).getFields().size()).isEqualTo(0);
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
    public void testProcessSetGroupFields() throws XFieldException {
        List<Locator> locators = createTestObjects();
        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        locatorSeeder.initialize();

        XField groupOneXField = xFieldHelper.createXField();
        xFieldHelper.addElement("x", "xv", groupOneXField);
        XField groupTwoXField = xFieldHelper.createXField();
        xFieldHelper.addElement("y", "yv", groupOneXField);

        given(locatorXFieldHelper.getXField(Locator.class.getName(), "g1"))
                .willReturn(groupOneXField);
        given(locatorXFieldHelper.getXField(Locator.class.getName(), "g2"))
                .willReturn(groupTwoXField);

        // when
        locatorSeeder.process();

        assertThat(locators.get(0).getXField()).isEqualTo(groupOneXField);
        assertThat(locators.get(1).getXField()).isEqualTo(groupOneXField);
        assertThat(locators.get(2).getXField()).isEqualTo(groupTwoXField);

        InOrder inOrder = inOrder(locatorXFieldHelper);
        inOrder.verify(locatorXFieldHelper).addLabel(locators.get(0));
        inOrder.verify(locatorXFieldHelper).addLabel(locators.get(1));
        inOrder.verify(locatorXFieldHelper).addLabel(locators.get(2));
    }

    @Test
    public void testHandover() throws XFieldException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        XField xField1 = xFieldHelper.createXField();
        xFieldHelper.addElement("x", "xv", xField1);
        XField xField2 = xFieldHelper.createXField();
        xFieldHelper.addElement("y", "yv", xField2);
        XField xField3 = xFieldHelper.createXField();
        xFieldHelper.addElement("z", "zv", xField3);

        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        locator1.setXField(xField1);
        Locator locator2 = locators.get(1);
        locator2.setXField(xField2);
        Locator locator3 = locators.get(2);
        locator3.setXField(xField3);

        LocatorSeeder locatorSeeder1 = dInjector.instance(LocatorSeeder.class);
        LocatorSeeder locatorSeeder2 = dInjector.instance(LocatorSeeder.class);
        LocatorSeeder locatorSeeder3 = dInjector.instance(LocatorSeeder.class);

        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        when(stepService.getStep(LocatorSeeder.class.getName()))
                .thenReturn(locatorSeeder1, locatorSeeder2, locatorSeeder3);

        locatorSeeder.initialize();

        locatorSeeder.handover();

        InOrder inOrder = inOrder(stepService);
        inOrder.verify(stepService).pushTask(locatorSeeder1, locator1,
                locator1.getXField());
        inOrder.verify(stepService).pushTask(locatorSeeder2, locator2,
                locator2.getXField());
        inOrder.verify(stepService).pushTask(locatorSeeder3, locator3,
                locator3.getXField());
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
