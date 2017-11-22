package org.codetab.gotz.step.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.appender.Appender;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.XFieldHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.AppenderService;
import org.codetab.gotz.step.StepState;
import org.codetab.gotz.testutil.XFieldBuilder;
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
 * CsvRecordEncoder Tests.
 * @author Maithilish
 *
 */
public class CsvRecordEncoderTest {

    @Mock
    private AppenderService appenderService;
    @Mock
    private ActivityService activityService;
    @Spy
    private XFieldHelper xFieldHelper;

    @InjectMocks
    private CsvRecordEncoder encoder;

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
        assertThat(encoder.instance()).isInstanceOf(CsvRecordEncoder.class);
        assertThat(encoder.instance()).isSameAs(encoder.instance());
    }

    @Test
    public void testProcessSortCol() throws InterruptedException {

        //@formatter:off
        XField a1 = new XFieldBuilder()
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

        encoder.setXField(a1);

        // col order field is set to non zero, row and fact order is 0
        Data data = getTestData();
        encoder.setInput(data);
        encoder.setStepType("encoder");

        encoder.initialize();

        Appender appender = Mockito.mock(Appender.class);

        given(appenderService.getAppender("x")).willReturn(appender);

        boolean actual = encoder.process();

        assertThat(actual).isTrue();
        assertThat(encoder.getStepState()).isEqualTo(StepState.PROCESS);

        InOrder inOrder = inOrder(appender);
        inOrder.verify(appender).append("l1|g1");
        inOrder.verify(appender).append(
                "item                           | c-0-value | c-1-value");
        inOrder.verify(appender).append(
                "r-0-value                      |f-0-0-value |f-1-0-value");
        inOrder.verify(appender).append(
                "r-1-value                      |f-0-1-value |f-1-1-value");
        inOrder.verify(appender).append(
                "r-2-value                      |f-0-2-value |f-1-2-value");
    }

    @Test
    public void testProcessNoLocatorNameFieldShouldThrowException() {

        encoder.setXField(new XField());
        encoder.setInput(new Data());

        // when
        try {
            encoder.process();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(XFieldException.class));
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
            assertThat(e.getMessage()).isEqualTo("xfield must not be null");
        }

        encoder.setXField(new XField());

        try {
            encoder.process();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("data must not be null");
        }
    }

    private Data getTestData() {
        // item | c-0-value | c-1-value
        // r-0-value |f-0-0-value |f-1-0-value
        // r-1-value |f-0-1-value |f-1-1-value
        // r-2-value |f-0-2-value |f-1-2-value

        Data data = new Data();
        data.setDataDef("dd");

        for (int c = 1; c >= 0; c--) {

            Axis col = new Axis();
            col.setName(AxisName.COL);
            col.setMatch("c-" + c + "-match");
            col.setValue("c-" + c + "-value");
            col.setOrder(c);

            for (int r = 2; r >= 0; r--) {
                Axis row = new Axis();
                row.setName(AxisName.ROW);
                row.setMatch("r-" + r + "-match");
                row.setValue("r-" + r + "-value");
                row.setOrder(r);

                Axis fact = new Axis();
                fact.setName(AxisName.FACT);
                fact.setMatch("f-" + c + "-" + r + "-match");
                fact.setValue("f-" + c + "-" + r + "-value");
                fact.setOrder(0);

                Member m = new Member();
                data.addMember(m);

                m.addAxis(col);
                m.addAxis(row);
                m.addAxis(fact);
            }

        }
        return data;
    }
}
