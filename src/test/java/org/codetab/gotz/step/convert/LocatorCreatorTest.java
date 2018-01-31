package org.codetab.gotz.step.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.helper.ThreadSleep;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Locator;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.model.helper.LocatorFieldsHelper;
import org.codetab.gotz.model.helper.LocatorHelper;
import org.codetab.gotz.shared.StepService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LocatorCreatorTest {

    @Mock
    private FieldsHelper fieldsHelper;
    @Mock
    private LocatorHelper locatorHelper;
    @Mock
    private LocatorFieldsHelper locatorFieldsHelper;
    @Mock
    private StepService stepService;
    @Mock
    private ThreadSleep threadSleep;

    @InjectMocks
    private LocatorCreator locatorCreator;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private Data data;
    private Labels labels;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        locatorCreator.setStepType("process");

        labels = new Labels("n", "g");
        locatorCreator.setLabels(labels);

        data = createTestData();
        locatorCreator.setInput(data);

        locatorCreator.setFields(TestUtil.createEmptyFields());
    }

    @Test
    public void testInstance() {
        locatorCreator = new LocatorCreator();

        assertThat(locatorCreator.isConsistent()).isFalse();
        assertThat(locatorCreator.getStepType()).isNull();
        assertThat(locatorCreator.instance())
                .isInstanceOf(LocatorCreator.class);
        assertThat(locatorCreator.instance())
                .isSameAs(locatorCreator.instance());
    }

    @Test
    public void testHandover() throws FieldsException, InvalidDataDefException {

        Member member1 = data.getMembers().get(0);
        Member member2 = data.getMembers().get(1);

        // new locator 1
        Labels labels1 = new Labels("n", "g1");
        Fields nextStepFields1 = TestUtil.createEmptyFields();

        Locator locator1 = new Locator();
        locator1.setName(labels1.getName());
        locator1.setGroup(labels1.getGroup());

        // new locator 2
        Labels labels2 = new Labels("n", "g2");
        Fields nextStepFields2 = TestUtil.createEmptyFields();

        Locator locator2 = new Locator();
        locator2.setName(labels2.getName());
        locator2.setGroup(labels2.getGroup());

        given(locatorFieldsHelper.getFields(locator1.getClass().getName(),
                locator1.getGroup())).willReturn(nextStepFields1);
        given(locatorHelper.createLabels(locator1)).willReturn(labels1);
        given(locatorHelper.createLocator(member1,
                locatorCreator.getLabels().getName(),
                locatorCreator.getLabel())).willReturn(locator1);

        given(locatorFieldsHelper.getFields(locator2.getClass().getName(),
                locator2.getGroup())).willReturn(nextStepFields2);
        given(locatorHelper.createLabels(locator2)).willReturn(labels2);
        given(locatorHelper.createLocator(member2,
                locatorCreator.getLabels().getName(),
                locatorCreator.getLabel())).willReturn(locator2);

        locatorCreator.handover();

        InOrder inOrder = inOrder(stepService, threadSleep);
        inOrder.verify(stepService).pushTask(locatorCreator, locator1, labels1,
                nextStepFields1);
        inOrder.verify(threadSleep).sleep(1000);
        inOrder.verify(stepService).pushTask(locatorCreator, locator2, labels2,
                nextStepFields2);
        inOrder.verify(threadSleep).sleep(1000);
        verifyNoMoreInteractions(stepService, threadSleep);
    }

    @Test
    public void testHandoverThrowsException()
            throws FieldsException, InvalidDataDefException {

        Member member1 = data.getMembers().get(0);

        // new locator 1
        Labels labels1 = new Labels("n", "g1");

        Locator locator1 = new Locator();
        locator1.setName(labels1.getName());
        locator1.setGroup(labels1.getGroup());

        given(locatorHelper.createLocator(member1,
                locatorCreator.getLabels().getName(),
                locatorCreator.getLabel())).willThrow(FieldsException.class)
                        .willThrow(InvalidDataDefException.class);
        try {
            locatorCreator.handover();
            fail("should throw exception");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(FieldsException.class);
        }

        try {
            locatorCreator.handover();
            fail("should throw exception");
        } catch (StepRunException e) {
            assertThat(e.getCause())
                    .isInstanceOf(InvalidDataDefException.class);
        }
    }

    @Test
    public void testCreateNextStepFieldsNoNodes()
            throws FieldsException, InvalidDataDefException {

        Member member1 = data.getMembers().get(0);

        // new locator 1
        Labels labels1 = new Labels("n", "g1");
        Fields nextStepFields1 = TestUtil.createEmptyFields();
        nextStepFields1.getNodes().clear(); // no nodes

        Locator locator1 = new Locator();
        locator1.setName(labels1.getName());
        locator1.setGroup(labels1.getGroup());

        given(locatorFieldsHelper.getFields(locator1.getClass().getName(),
                locator1.getGroup())).willReturn(nextStepFields1);
        given(locatorHelper.createLabels(locator1)).willReturn(labels1);
        given(locatorHelper.createLocator(member1,
                locatorCreator.getLabels().getName(),
                locatorCreator.getLabel())).willReturn(locator1);

        testRule.expect(StepRunException.class);
        locatorCreator.handover();
    }

    @Test
    public void testStore() {
        assertThat(locatorCreator.store()).isFalse();
    }

    @Test
    public void testProcess() {
        assertThat(locatorCreator.process()).isFalse();
    }

    private Data createTestData() {

        Data testData = new Data();

        Fields fields = TestUtil.createEmptyFields();

        Axis fact = new Axis();
        fact.setName(AxisName.FACT);
        fact.setIndex(1);
        fact.setOrder(1);
        fact.setValue("locator-url-1");
        fact.setFields(fields);

        Member member = new Member();
        member.addAxis(fact);

        testData.addMember(member);

        fact = new Axis();
        fact.setName(AxisName.FACT);
        fact.setIndex(1);
        fact.setOrder(1);
        fact.setValue("locator-url-2");
        fact.setFields(fields);

        member = new Member();
        member.addAxis(fact);

        testData.addMember(member);

        return testData;
    }
}
