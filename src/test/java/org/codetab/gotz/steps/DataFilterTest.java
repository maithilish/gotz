package org.codetab.gotz.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DataFilter tests.
 * @author Maithilish
 *
 */
public class DataFilterTest {

    @Mock
    private DataDefService dataDefService;
    @Mock
    private ActivityService activityService;

    @InjectMocks
    private DataFilter filter;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInstance() {
        assertThat(filter.isConsistent()).isFalse();
        assertThat(filter.getStepType()).isNull();
        assertThat(filter.instance()).isInstanceOf(DataFilter.class);
        assertThat(filter.instance()).isSameAs(filter.instance());
    }

    @Test
    public void testProcessFilterOnValue()
            throws IllegalArgumentException, DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        Map<AxisName, List<FieldsBase>> filterMap =
                getTestFilterMap(AxisName.COL, "value", "m0-col-value");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();
        removeMember(expectedData, "m0-col-value");

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterOnMatch()
            throws IllegalArgumentException, DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        Map<AxisName, List<FieldsBase>> filterMap =
                getTestFilterMap(AxisName.COL, "match", "m1-col-match");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();
        removeMember(expectedData, "m1-col-match");

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterNoMatchingItem()
            throws IllegalArgumentException, DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        Map<AxisName, List<FieldsBase>> filterMap =
                getTestFilterMap(AxisName.COL, "value", "mx-col-match");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterAxisValueMismatch()
            throws IllegalArgumentException, DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        // axis - row but value is from col axis
        Map<AxisName, List<FieldsBase>> filterMap =
                getTestFilterMap(AxisName.ROW, "value", "m1-col-value");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterUnknowAxis()
            throws IllegalArgumentException, DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        // fact axis not in test data
        Map<AxisName, List<FieldsBase>> filterMap =
                getTestFilterMap(AxisName.FACT, "value", "m1-col-value");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterTwoMembers()
            throws IllegalArgumentException, DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        Map<AxisName, List<FieldsBase>> filterMap =
                getTestFilterMap(AxisName.COL, "value", "m0-col-value");
        filterMap.putAll(
                getTestFilterMap(AxisName.ROW, "match", "m2-row-match"));
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();
        removeMember(expectedData, "m0-col-value");
        removeMember(expectedData, "m2-row-match");

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessIllegalState() {
        // step input is null
        testRule.expect(IllegalStateException.class);
        filter.process();
    }

    @Test
    public void testProcessNoFilterMapShouldThrowException()
            throws DataDefNotFoundException {
        Data data = getTestData();
        filter.setInput(data);

        given(dataDefService.getFilterMap("dd"))
                .willThrow(DataDefNotFoundException.class);

        // when
        try {
            filter.process();
        } catch (StepRunException e) {
            verify(activityService).addActivity(eq(Type.GIVENUP),
                    any(String.class), any(DataDefNotFoundException.class));
        }

        testRule.expect(StepRunException.class);
        filter.process();
    }

    private Data getTestData() {

        Data data = new Data();
        data.setDataDef("dd");

        for (int i = 0; i < 4; i++) {
            Axis col = new Axis();
            col.setName(AxisName.COL);
            col.setMatch("m" + i + "-col-match");
            col.setValue("m" + i + "-col-value");

            Axis row = new Axis();
            row.setName(AxisName.ROW);
            row.setMatch("m" + i + "-row-match");
            row.setValue("m" + i + "-row-value");

            Member m = new Member();
            m.addAxis(col);
            m.addAxis(row);
            data.addMember(m);
        }

        // null value and match member
        Axis col = new Axis();
        col.setName(AxisName.COL);

        Axis row = new Axis();
        row.setName(AxisName.ROW);

        Member m = new Member();
        m.addAxis(col);
        m.addAxis(row);
        data.addMember(m);

        return data;
    }

    private Map<AxisName, List<FieldsBase>> getTestFilterMap(
            final AxisName axis, final String group,
            final String... filterStrings) {

        Fields groupFields = new Fields();
        groupFields.setName("group");
        groupFields.setValue(group);

        for (String filterString : filterStrings) {
            Field f = TestUtil.createField("f", filterString);
            groupFields.getFields().add(f);
        }

        Map<AxisName, List<FieldsBase>> filterMap = new HashMap<>();

        filterMap.put(axis, TestUtil.asList(groupFields));
        return filterMap;
    }

    // to get expected data from test data - crude filter, removes last
    // matching member
    private void removeMember(final Data data, final String filterString) {
        Member removeMember = null;
        for (Member member : data.getMembers()) {
            for (Axis axis : member.getAxes()) {
                if (axis.getMatch() == null && axis.getValue() == null) {
                    continue;
                }
                if (axis.getValue().equals(filterString)
                        || axis.getMatch().equals(filterString)) {
                    removeMember = member;
                }
            }
        }
        data.getMembers().remove(removeMember);
    }
}
