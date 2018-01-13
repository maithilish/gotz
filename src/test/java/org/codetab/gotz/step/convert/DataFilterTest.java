package org.codetab.gotz.step.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.gotz.exception.DataDefNotFoundException;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.Axis;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.Data;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.Labels;
import org.codetab.gotz.model.Member;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.shared.ActivityService;
import org.codetab.gotz.shared.DataDefService;
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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
    @Spy
    private FieldsHelper fieldsHelper;

    @InjectMocks
    private DataFilter filter;

    private Data data;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Labels labels = new Labels("x", "y");
        filter.setLabels(labels);

        data = getTestData();
        filter.setInput(data);
    }

    @Test
    public void testInstance() {
        assertThat(filter.isConsistent()).isFalse();
        assertThat(filter.getStepType()).isNull();
        assertThat(filter.instance()).isInstanceOf(DataFilter.class);
        assertThat(filter.instance()).isSameAs(filter.instance());
    }

    @Test
    public void testProcessFilterOnValue() throws IllegalArgumentException,
            DataDefNotFoundException, FieldsException {

        Map<AxisName, Fields> filterMap =
                getTestFilterMap(AxisName.COL, "value", "m0-col-value");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();
        removeMember(expectedData, "m0-col-value");

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterOnMatch() throws IllegalArgumentException,
            DataDefNotFoundException, FieldsException {

        Map<AxisName, Fields> filterMap =
                getTestFilterMap(AxisName.COL, "match", "m1-col-match");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();
        removeMember(expectedData, "m1-col-match");

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterNoMatchingItem()
            throws IllegalArgumentException, DataDefNotFoundException,
            FieldsException {

        Map<AxisName, Fields> filterMap =
                getTestFilterMap(AxisName.COL, "value", "mx-col-match");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterAxisValueMismatch()
            throws IllegalArgumentException, DataDefNotFoundException,
            FieldsException {

        // axis - row but value is from col axis
        Map<AxisName, Fields> filterMap =
                getTestFilterMap(AxisName.ROW, "value", "m1-col-value");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterUnknowAxis() throws IllegalArgumentException,
            DataDefNotFoundException, FieldsException {

        // fact axis not in test data
        Map<AxisName, Fields> filterMap =
                getTestFilterMap(AxisName.FACT, "value", "m1-col-value");
        given(dataDefService.getFilterMap("dd")).willReturn(filterMap);

        filter.process();

        Data expectedData = getTestData();

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    public void testProcessFilterTwoMembers() throws IllegalArgumentException,
            DataDefNotFoundException, FieldsException {

        Map<AxisName, Fields> filterMap =
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
    public void testProcessIllegalState() throws IllegalAccessException {
        // step input is null
        FieldUtils.writeField(filter, "data", null, true);
        testRule.expect(IllegalStateException.class);
        filter.process();
    }

    @Test
    public void testProcessNoFilterMapShouldThrowException()
            throws DataDefNotFoundException {
        given(dataDefService.getFilterMap("dd"))
                .willThrow(DataDefNotFoundException.class);

        // when
        try {
            filter.process();
        } catch (StepRunException e) {
            assertThat(e.getCause())
                    .isInstanceOf(DataDefNotFoundException.class);
        }

        testRule.expect(StepRunException.class);
        filter.process();
    }

    private Data getTestData() {

        Data testData = new Data();
        testData.setDataDef("dd");

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
            testData.addMember(m);
        }

        // null value and match member
        Axis col = new Axis();
        col.setName(AxisName.COL);

        Axis row = new Axis();
        row.setName(AxisName.ROW);

        Member m = new Member();
        m.addAxis(col);
        m.addAxis(row);
        testData.addMember(m);

        return testData;
    }

    private Map<AxisName, Fields> getTestFilterMap(final AxisName axis,
            final String group, final String... filterStrings)
            throws FieldsException {

        //@formatter:off
        XOBuilder<Fields> xo = new XOBuilder<Fields>()
          .add("<xf:fields>")
          .add("<xf:filters type='")
          .add(group)
          .add("'>");

        for (String filterString : filterStrings) {
            xo.add("<xf:filter name='f' pattern='")
            .add(filterString)
            .add("' />");
        }

        xo.add("</xf:filters>")
          .add("</xf:fields>");
        //@formatter:on

        // need fields with filters as root so calling build() instead of
        // buildFields()
        Fields fields = xo.build(Fields.class).get(0);

        Map<AxisName, Fields> filterMap = new HashMap<>();

        filterMap.put(axis, fields);
        return filterMap;
    }

    // to get expected data from test data - crude filter, removes last
    // matching member
    private void removeMember(final Data dataToFilter,
            final String filterString) {
        Member removeMember = null;
        for (Member member : dataToFilter.getMembers()) {
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
        dataToFilter.getMembers().remove(removeMember);
    }
}
