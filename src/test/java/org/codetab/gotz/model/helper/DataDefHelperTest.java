package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.util.Date;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * <p>
 * DataDefHelper tests.
 * @author Maithilish
 *
 */
public class DataDefHelperTest {

    @Mock
    private ConfigService configService;
    @Spy
    private XFieldHelper xFieldHelper;
    @InjectMocks
    private DataDefHelper dataDefHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddFact() throws XFieldException {

        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefHelper.addFact(dataDef);

        // then
        DMember fact = new DMember();
        fact.setAxis("fact");
        fact.setName("fact");
        fact.setOrder(0);
        fact.setValue(null);
        fact.setXfield(TestUtil.buildXField("", "xf")); // only xfield element

        DAxis factAxis = getAxis(dataDef, "fact");
        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        assertThat(factAxis.getMember().size()).isEqualTo(1);
        assertThat(factAxis.getMember()).contains(fact);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(rowAxis.getMember().size()).isEqualTo(1);
    }

    @Test
    public void testAddFactNullParams() throws XFieldException {
        try {
            dataDefHelper.addFact(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testSetOrder() throws XFieldException {
        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefHelper.setOrder(dataDef);

        // then
        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        DMember m1 = new DMember();
        m1.setAxis("col");
        m1.setOrder(0);
        m1.setXfield(TestUtil.createXField("xf"));
        DMember m2 = new DMember();
        m2.setAxis("col");
        m2.setOrder(1);
        m2.setXfield(TestUtil.createXField("xf"));
        DMember m3 = new DMember();
        m3.setAxis("row");
        m3.setOrder(10);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(colAxis.getMember()).contains(m1);
        assertThat(colAxis.getMember()).contains(m2);

        assertThat(rowAxis.getMember().size()).isEqualTo(1);
        assertThat(rowAxis.getMember()).contains(m3);
    }

    @Test
    public void testSetOrderNullParams() {
        try {
            dataDefHelper.setOrder(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testAddIndexRange() throws XFieldException {
        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then
        XField xField =
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf");

        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        DMember m1 = new DMember();
        m1.setXfield(xField);
        DMember m2 = new DMember();
        m2.setXfield(xField);
        DMember m3 = new DMember();
        m3.setOrder(10);
        m3.setXfield(xField);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(colAxis.getMember()).contains(m1);
        assertThat(colAxis.getMember()).contains(m2);

        assertThat(rowAxis.getMember().size()).isEqualTo(1);
        assertThat(rowAxis.getMember()).contains(m3);
    }

    @Test
    public void testAddIndexRangeWithIndex() throws XFieldException {
        // given
        DMember m1 = new DMember();
        m1.setIndex(5);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then
        XField xField = TestUtil.createXField("indexRange", "5-5");
        m1.setXfield(xField);

        assertThat(colAxis.getMember().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeWithIndexRange() throws XFieldException {
        // given
        XField xField =
                TestUtil.buildXField("<xf:indexRange value='3-10' />", "xf");

        DMember m1 = new DMember();
        m1.setXfield(xField);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then
        assertThat(m1.getXfield().getNodes().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeWithBreakAfter() throws XFieldException {
        // given
        XField xField =
                TestUtil.buildXField("<xf:breakAfter value='xyz' />", "xf");

        DMember m1 = new DMember();
        m1.setXfield(xField);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then
        assertThat(m1.getXfield().getNodes().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeNullParams() throws XFieldException {
        try {
            dataDefHelper.addIndexRange(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testSetDates() throws XFieldException {
        // given
        DataDef dataDef = createTestDataDef();

        Date runDate = new Date();
        Date highDate = DateUtils.addYears(runDate, 50);

        given(configService.getRunDateTime()).willReturn(runDate);
        given(configService.getHighDate()).willReturn(highDate);

        // when
        dataDefHelper.setDates(dataDef);

        // then
        assertThat(dataDef.getFromDate()).isEqualTo(runDate);
        assertThat(dataDef.getToDate()).isEqualTo(highDate);
    }

    @Test
    public void testSetDatesIllegalState() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(dataDefHelper, "configService", null,
                true);
        try {
            dataDefHelper.setDates(new DataDef());
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("configService is null");
        }
    }

    @Test
    public void testSetDatesNullParams() {
        try {
            dataDefHelper.setDates(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testGetAxis() throws XFieldException {
        DataDef dataDef = createTestDataDef();

        DAxis actual = dataDefHelper.getAxis(dataDef, AxisName.COL);

        assertThat(actual).isEqualTo(getAxis(dataDef, "col"));

        actual = dataDefHelper.getAxis(dataDef, AxisName.ROW);

        assertThat(actual).isEqualTo(getAxis(dataDef, "row"));
    }

    @Test
    public void testGetAxisIgnoreCase() throws XFieldException {
        DataDef dataDef = createTestDataDef();
        DAxis axis = getAxis(dataDef, "row");
        axis.setName("Row");

        DAxis actual = dataDefHelper.getAxis(dataDef, AxisName.ROW);

        assertThat(actual).isEqualTo(getAxis(dataDef, "Row"));
    }

    @Test
    public void testGetAxisFromEmptyList() throws XFieldException {
        DataDef dataDef = createTestDataDef();
        dataDef.getAxis().clear();

        DAxis actual = dataDefHelper.getAxis(dataDef, AxisName.COL);

        assertThat(actual).isNull();
    }

    @Test
    public void testGetAxisNullParams() {
        try {
            dataDefHelper.getAxis(null, AxisName.COL);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }

        try {
            dataDefHelper.getAxis(new DataDef(), null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("axisName must not be null");
        }
    }

    private DAxis getAxis(final DataDef dataDef, final String axisName) {
        return dataDef.getAxis().stream()
                .filter(d -> d.getName().equals(axisName)).findFirst().get();
    }

    private DataDef createTestDataDef() throws XFieldException {

        DMember m1 = new DMember();
        DMember m2 = new DMember();
        m2.setAxis("x");
        m1.setXfield(TestUtil.createXField("xf"));
        m2.setXfield(TestUtil.createXField("xf"));

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);
        colAxis.getMember().add(m2);

        DMember m3 = new DMember();
        m3.setOrder(10);

        DAxis rowAxis = new DAxis();
        rowAxis.setName("row");
        rowAxis.getMember().add(m3);

        DAxis factAxis = new DAxis();
        factAxis.setName("fact");

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);
        dataDef.getAxis().add(rowAxis);
        dataDef.getAxis().add(factAxis);

        return dataDef;
    }

}
