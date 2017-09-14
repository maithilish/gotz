package org.codetab.gotz.step.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.step.extract.LocatorSeeder;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    public void testProcess() {
        List<FieldsBase> fields =
                TestUtil.asList(TestUtil.createField("x", "xv"));
        List<Locator> locators = new ArrayList<>();

        given(fieldsHelper.getStepFields()).willReturn(fields);
        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        locatorSeeder.initialize();

        // when
        boolean actual = locatorSeeder.process();

        // then
        assertThat(actual).isTrue();
        assertThat(locatorSeeder.getStepState()).isEqualTo(StepState.PROCESS);
        assertThat(locatorSeeder.isConsistent()).isTrue();

        assertThat(locatorSeeder.getFields()).isEqualTo(fields);
    }

    @Test
    public void testProcessSetGroupFields() {
        List<Locator> locators = createTestObjects();
        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        locatorSeeder.initialize();

        List<FieldsBase> groupOneFields =
                TestUtil.asList(TestUtil.createField("x", "xv"));
        List<FieldsBase> groupTwoFields =
                TestUtil.asList(TestUtil.createField("y", "yv"));

        given(fieldsHelper.getLocatorGroupFields("g1"))
                .willReturn(groupOneFields);
        given(fieldsHelper.getLocatorGroupFields("g2"))
                .willReturn(groupTwoFields);

        // when
        locatorSeeder.process();

        assertThat(locators.get(0).getFields()).isEqualTo(groupOneFields);
        assertThat(locators.get(1).getFields()).isEqualTo(groupOneFields);
        assertThat(locators.get(2).getFields()).isEqualTo(groupTwoFields);

        InOrder inOrder = inOrder(fieldsHelper);
        inOrder.verify(fieldsHelper).addLabel(locators.get(0));
        inOrder.verify(fieldsHelper).addLabel(locators.get(1));
        inOrder.verify(fieldsHelper).addLabel(locators.get(2));
    }

    @Test
    public void testHandover() {
        List<Locator> locators = createTestObjects();
        Locator locator1 = locators.get(0);
        locator1.getFields().add(TestUtil.createField("x", "xv"));
        Locator locator2 = locators.get(1);
        locator2.getFields().add(TestUtil.createField("y", "yv"));
        Locator locator3 = locators.get(2);
        locator3.getFields().add(TestUtil.createField("z", "zv"));

        given(locatorHelper.getLocatorsFromBeans()).willReturn(locators);
        locatorSeeder.initialize();

        locatorSeeder.handover();

        InOrder inOrder = inOrder(stepService);
        inOrder.verify(stepService).pushTask(locatorSeeder, locator1,
                locator1.getFields());
        inOrder.verify(stepService).pushTask(locatorSeeder, locator2,
                locator2.getFields());
        inOrder.verify(stepService).pushTask(locatorSeeder, locator3,
                locator3.getFields());

        verifyNoMoreInteractions(stepService);
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
