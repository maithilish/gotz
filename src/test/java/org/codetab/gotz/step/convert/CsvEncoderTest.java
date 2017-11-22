package org.codetab.gotz.step.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.testutil.FieldsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * <p>
 * CsvEncoder Tests.
 * @author Maithilish
 *
 */
public class CsvEncoderTest {

    @Mock
    private AppenderService appenderService;
    @Mock
    private ActivityService activityService;
    @Spy
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private CsvEncoder encoder;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstance() {
        assertThat(encoder.isConsistent()).isFalse();
        assertThat(encoder.getStepType()).isNull();
        assertThat(encoder.instance()).isInstanceOf(CsvEncoder.class);
        assertThat(encoder.instance()).isSameAs(encoder.instance());
    }

    @Test
    public void testProcessSortCol() throws InterruptedException {

        //@formatter:off
        Fields a1 = new FieldsBuilder()
                .add("<task>")
                .add(" <steps>")
                .add("  <step name='encoder'>")
                .add("     <appender>")
                .add("        <name>x</name>")
                .add("     </appender>")
                .add("  </step>")
                .add(" </steps>")
                .add("</task>")
                .add("<locatorName>l1</locatorName>")
                .add("<locatorGroup>g1</locatorGroup>")
                .build(null); // default ns
        //@formatter:on

        encoder.setFields(a1);

        // col order field is set to non zero, row and fact order is 0
        Data data = getTestData(AxisName.COL);
        encoder.setInput(data);
        encoder.setStepType("encoder");

        encoder.initialize();

        Appender appender = Mockito.mock(Appender.class);

        given(appenderService.getAppender("x")).willReturn(appender);

        boolean actual = encoder.process();

        assertThat(actual).isTrue();
        assertThat(encoder.getStepState()).isEqualTo(StepState.PROCESS);

        InOrder inOrder = inOrder(appender);
        inOrder.verify(appender)
                .append("l1 |g1 |m0-col-value |m0-row-value |m0-fact-value");
        inOrder.verify(appender)
                .append("l1 |g1 |m1-col-value |m1-row-value |m1-fact-value");
        inOrder.verify(appender)
                .append("l1 |g1 |m2-col-value |m2-row-value |m2-fact-value");
        inOrder.verify(appender)
                .append("l1 |g1 |m3-col-value |m3-row-value |m3-fact-value");
    }

    @Test
    public void testProcessSortRow() throws InterruptedException {

        //@formatter:off
        Fields a1 = new FieldsBuilder()
                .add("<task>")
                .add(" <steps>")
                .add("  <step name='encoder'>")
                .add("     <appender>")
                .add("        <name>x</name>")
                .add("     </appender>")
                .add("  </step>")
                .add(" </steps>")
                .add("</task>")
                .add("<locatorName>l1</locatorName>")
                .add("<locatorGroup>g1</locatorGroup>")
                .build(null);  // default ns
        //@formatter:on

        encoder.setFields(a1);

        // row order field is set to non zero, col and fact order is 0
        Data data = getTestData(AxisName.ROW);
        encoder.setInput(data);
        encoder.setStepType("encoder");

        encoder.initialize();

        Appender appender = Mockito.mock(Appender.class);

        given(appenderService.getAppender("x")).willReturn(appender);

        boolean actual = encoder.process();

        assertThat(actual).isTrue();
        assertThat(encoder.getStepState()).isEqualTo(StepState.PROCESS);

        InOrder inOrder = inOrder(appender);
        inOrder.verify(appender)
                .append("l1 |g1 |m0-col-value |m0-row-value |m0-fact-value");
        inOrder.verify(appender)
                .append("l1 |g1 |m1-col-value |m1-row-value |m1-fact-value");
        inOrder.verify(appender)
                .append("l1 |g1 |m2-col-value |m2-row-value |m2-fact-value");
        inOrder.verify(appender)
                .append("l1 |g1 |m3-col-value |m3-row-value |m3-fact-value");
    }

    @Test
    public void testProcessNoLocatorNameFieldShouldThrowException() {

        encoder.setFields(new Fields());
        encoder.setInput(new Data());

        // when
        try {
            encoder.process();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(FieldsException.class));
        }

        testRule.expect(StepRunException.class);
        encoder.process();
    }

    @Test
    public void testInitializeIllegalState() {
        try {
            encoder.process();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("fields must not be null");
        }

        encoder.setFields(new Fields());

        try {
            encoder.process();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("data must not be null");
        }
    }

    // based on setOrder param, order value is set to order field of either col
    // or row. Fact order is always zero.
    private Data getTestData(final AxisName setOrder) {

        Data data = new Data();
        data.setDataDef("dd");

        for (int i = 3; i >= 0; i--) {
            // swap some order values
            int order = i;
            if (order == 2) {
                order = 1;
            } else if (order == 1) {
                order = 2;
            }

            Axis col = new Axis();
            col.setName(AxisName.COL);
            col.setMatch("m" + order + "-col-match");
            col.setValue("m" + order + "-col-value");
            if (setOrder.equals(AxisName.COL)) {
                col.setOrder(order);
            } else {
                col.setOrder(0);
            }

            Axis row = new Axis();
            row.setName(AxisName.ROW);
            row.setMatch("m" + order + "-row-match");
            row.setValue("m" + order + "-row-value");
            if (setOrder.equals(AxisName.ROW)) {
                row.setOrder(order);
            } else {
                row.setOrder(0);
            }

            Axis fact = new Axis();
            fact.setName(AxisName.FACT);
            fact.setMatch("m" + order + "-fact-match");
            fact.setValue("m" + order + "-fact-value");
            fact.setOrder(0);

            Member m = new Member();
            m.addAxis(col);
            m.addAxis(row);
            m.addAxis(fact);
            data.addMember(m);
        }
        return data;
    }
}
