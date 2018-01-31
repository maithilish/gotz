package org.codetab.gotz.step.load.encoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import java.util.Arrays;
import java.util.List;

import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.step.load.encoder.helper.EncoderHelper;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CsvEncoderTest {

    @Mock
    private EncoderHelper encoderHelper;
    @InjectMocks
    private CsvEncoder encoder;

    private Fields fields;
    private Labels labels;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        fields = TestUtil.createEmptyFields();
        encoder.setFields(fields);

        labels = new Labels("n", "g");
        encoder.setLabels(labels);
    }

    @Test
    public void testEncode() throws Exception {
        Data data = createTestData();

        given(encoderHelper.getDelimiter(fields)).willReturn("#");

        List<String> actual = encoder.encode(data);

        List<String> expected = Arrays.asList("n#g#m0-cv#m0-rv#m-0-fv",
                "n#g#m1-cv#m1-rv#m-1-fv");
        assertThat(actual).isEqualTo(expected);

        // test that sort is called before other things
        InOrder inOrder = inOrder(encoderHelper);
        inOrder.verify(encoderHelper).sort(data, fields);
        inOrder.verify(encoderHelper).getDelimiter(fields);
    }

    @Test
    public void testEncodeIllegalState() throws Exception {

        encoder = new CsvEncoder();
        Data data = null;

        try {
            encoder.encode(data);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("fields must not be null");
        }

        encoder.setFields(TestUtil.createEmptyFields());

        try {
            encoder.encode(data);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("data must not be null");
        }
    }

    private Data createTestData() {

        Data data = new Data();
        for (int c = 0; c < 2; c++) {
            Axis col = new Axis();
            col.setName(AxisName.COL);
            col.setValue("m" + c + "-cv");
            Axis row = new Axis();
            row.setName(AxisName.ROW);
            row.setValue("m" + c + "-rv");
            Axis fact = new Axis();
            fact.setName(AxisName.FACT);
            fact.setValue("m-" + c + "-fv");

            Member member = new Member();
            member.addAxis(col);
            member.addAxis(row);
            member.addAxis(fact);
            data.addMember(member);
        }

        return data;
    }
}
