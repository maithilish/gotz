package org.codetab.gotz.step.load.encoder.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EncoderHelperTest {

    @Mock
    private FieldsHelper fieldsHelper;
    @Mock
    private ActivityService activityService;
    @InjectMocks
    private EncoderHelper encoderHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSort() {

        Fields fields = TestUtil.createEmptyFields();

        Data data = createTestData();
        Member member1 = data.getMembers().get(0);
        member1.getAxis(AxisName.COL).setOrder(2);
        member1.getAxis(AxisName.ROW).setOrder(3);

        Member member2 = data.getMembers().get(1);
        member2.getAxis(AxisName.COL).setOrder(1);
        member2.getAxis(AxisName.ROW).setOrder(2);

        Member member3 = data.getMembers().get(2);
        member3.getAxis(AxisName.COL).setOrder(3);
        member3.getAxis(AxisName.ROW).setOrder(1);

        String xpath = "/xf:fields/xf:encoder/xf:sortOrder";
        given(fieldsHelper.isDefined(xpath, true, fields)).willReturn(true);

        given(fieldsHelper.getValue(xpath, fields)).willReturn("col");

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member2);
        assertThat(data.getMembers().get(1)).isSameAs(member1);
        assertThat(data.getMembers().get(2)).isSameAs(member3);

        given(fieldsHelper.getValue(xpath, fields)).willReturn("row");

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member3);
        assertThat(data.getMembers().get(1)).isSameAs(member2);
        assertThat(data.getMembers().get(2)).isSameAs(member1);

        given(fieldsHelper.getValue(xpath, fields)).willReturn("col,row");

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member3);
        assertThat(data.getMembers().get(1)).isSameAs(member2);
        assertThat(data.getMembers().get(2)).isSameAs(member1);

        given(fieldsHelper.getValue(xpath, fields)).willReturn("row,col");

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member2);
        assertThat(data.getMembers().get(1)).isSameAs(member1);
        assertThat(data.getMembers().get(2)).isSameAs(member3);

        // fact has no comparator
        given(fieldsHelper.getValue(xpath, fields)).willReturn("col,fact,row");

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member3);
        assertThat(data.getMembers().get(1)).isSameAs(member2);
        assertThat(data.getMembers().get(2)).isSameAs(member1);

        // test whether axis name trimmed
        given(fieldsHelper.getValue(xpath, fields))
                .willReturn("col , row, fact ");

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member3);
        assertThat(data.getMembers().get(1)).isSameAs(member2);
        assertThat(data.getMembers().get(2)).isSameAs(member1);
    }

    @Test
    public void testSortSortOrderNotDefined() {

        Fields fields = TestUtil.createEmptyFields();

        Data data = createTestData();
        Member member1 = data.getMembers().get(0);
        member1.getAxis(AxisName.COL).setOrder(2);
        member1.getAxis(AxisName.ROW).setOrder(3);

        Member member2 = data.getMembers().get(1);
        member2.getAxis(AxisName.COL).setOrder(1);
        member2.getAxis(AxisName.ROW).setOrder(2);

        Member member3 = data.getMembers().get(2);
        member3.getAxis(AxisName.COL).setOrder(3);
        member3.getAxis(AxisName.ROW).setOrder(1);

        String xpath = "/xf:fields/xf:encoder/xf:sortOrder";
        given(fieldsHelper.isDefined(xpath, true, fields)).willReturn(false);

        encoderHelper.sort(data, fields);

        assertThat(data.getMembers().get(0)).isSameAs(member3);
        assertThat(data.getMembers().get(1)).isSameAs(member2);
        assertThat(data.getMembers().get(2)).isSameAs(member1);
    }

    @Test
    public void testSortSortOrderFieldsBlank() {

        Fields fields = TestUtil.createEmptyFields();

        Data data = createTestData();

        String xpath = "/xf:fields/xf:encoder/xf:sortOrder";
        given(fieldsHelper.isDefined(xpath, true, fields)).willReturn(true);

        given(fieldsHelper.getValue(xpath, fields)).willReturn(" ");

        encoderHelper.sort(data, fields);

        verify(activityService).addActivity(eq(Type.WARN), any(String.class));
    }

    @Test
    public void testSortInvalidAxisThrowsException() {

        Fields fields = TestUtil.createEmptyFields();

        Data data = createTestData();
        Member member1 = data.getMembers().get(0);
        member1.getAxis(AxisName.COL).setOrder(2);
        member1.getAxis(AxisName.ROW).setOrder(3);

        Member member2 = data.getMembers().get(1);
        member2.getAxis(AxisName.COL).setOrder(1);
        member2.getAxis(AxisName.ROW).setOrder(2);

        Member member3 = data.getMembers().get(2);
        member3.getAxis(AxisName.COL).setOrder(3);
        member3.getAxis(AxisName.ROW).setOrder(1);

        String xpath = "/xf:fields/xf:encoder/xf:sortOrder";
        given(fieldsHelper.isDefined(xpath, true, fields)).willReturn(true);

        given(fieldsHelper.getValue(xpath, fields)).willReturn("row,xyz,col");

        // assert member order not changed
        try {
            encoderHelper.sort(data, fields);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(data.getMembers().get(0)).isSameAs(member1);
            assertThat(data.getMembers().get(1)).isSameAs(member2);
            assertThat(data.getMembers().get(2)).isSameAs(member3);
        }
    }

    @Test
    public void testGetDelimiter() throws FieldsNotFoundException {
        Fields fields = TestUtil.createEmptyFields();

        given(fieldsHelper.getLastValue("/xf:fields/xf:encoder/xf:delimiter",
                fields)).willReturn(",");

        String actual = encoderHelper.getDelimiter(fields);

        assertThat(actual).isEqualTo(",");

        // default delimiter |
        given(fieldsHelper.getLastValue("/xf:fields/xf:encoder/xf:delimiter",
                fields)).willThrow(FieldsNotFoundException.class);

        actual = encoderHelper.getDelimiter(fields);

        assertThat(actual).isEqualTo("|");
    }

    private Data createTestData() {

        Data data = new Data();
        for (int c = 0; c < 3; c++) {
            Axis col = new Axis();
            col.setName(AxisName.COL);
            col.setValue("m" + c + "-cv");
            col.setOrder(c);
            Axis row = new Axis();
            row.setName(AxisName.ROW);
            row.setValue("m" + c + "-rv");
            row.setOrder(c);
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
