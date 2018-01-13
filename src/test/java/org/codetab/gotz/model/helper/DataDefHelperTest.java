package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.util.Date;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.shared.ConfigService;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XOBuilder;
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
    private FieldsHelper fieldsHelper;
    @InjectMocks
    private DataDefHelper dataDefHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddFact() throws FieldsException {

        Fields fields = TestUtil.createEmptyFields();

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
        fact.setFields(fields); // only fields element

        DAxis factAxis = getAxis(dataDef, "fact");
        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        assertThat(factAxis.getMember().size()).isEqualTo(1);
        assertThat(factAxis.getMember()).contains(fact);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(rowAxis.getMember().size()).isEqualTo(1);
    }

    @Test
    public void testAddFactNullParams() throws FieldsException {
        try {
            dataDefHelper.addFact(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testSetOrder() throws FieldsException {
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
        m1.setFields(TestUtil.createEmptyFields());
        DMember m2 = new DMember();
        m2.setAxis("col");
        m2.setOrder(1);
        m2.setFields(TestUtil.createEmptyFields());
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
    public void testAddIndexRange() throws FieldsException {

        // given
        DataDef dataDef = createTestDataDef();

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:indexRange value='1-1' />")
          .buildFields();
        //@formatter:on

        DAxis colAxis = getAxis(dataDef, "col");
        DAxis rowAxis = getAxis(dataDef, "row");

        DMember m1 = new DMember();
        m1.setFields(fields);
        DMember m2 = new DMember();
        m2.setFields(fields);
        DMember m3 = new DMember();
        m3.setOrder(10);
        m3.setFields(fields);

        assertThat(colAxis.getMember().size()).isEqualTo(2);
        assertThat(colAxis.getMember()).contains(m1);
        assertThat(colAxis.getMember()).contains(m2);

        assertThat(rowAxis.getMember().size()).isEqualTo(1);
        assertThat(rowAxis.getMember()).contains(m3);
    }

    @Test
    public void testAddIndexRangeWithIndex() throws FieldsException {
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

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:indexRange value='5-5' />")
          .buildFields();
        //@formatter:on

        m1.setFields(fields);

        assertThat(colAxis.getMember().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeWithIndexRange() throws FieldsException {
        // given

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:indexRange value='3-10' />")
          .buildFields();
        //@formatter:on

        DMember m1 = new DMember();
        m1.setFields(fields);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then
        assertThat(m1.getFields().getNodes().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeWithBreakAfter() throws FieldsException {
        // given

        //@formatter:off
        Fields fields = new XOBuilder<Fields>()
          .add("<xf:breakAfter value='xyz' />")
          .buildFields();
        //@formatter:on

        DMember m1 = new DMember();
        m1.setFields(fields);

        DAxis colAxis = new DAxis();
        colAxis.setName("col");
        colAxis.getMember().add(m1);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(colAxis);

        // when
        dataDefHelper.addIndexRange(dataDef);

        // then
        assertThat(m1.getFields().getNodes().size()).isEqualTo(1);
        assertThat(colAxis.getMember()).contains(m1);
    }

    @Test
    public void testAddIndexRangeNullParams() throws FieldsException {
        try {
            dataDefHelper.addIndexRange(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testSetDates() throws FieldsException {
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
    public void testGetAxis() throws FieldsException {
        DataDef dataDef = createTestDataDef();

        DAxis actual = dataDefHelper.getAxis(dataDef, AxisName.COL);

        assertThat(actual).isEqualTo(getAxis(dataDef, "col"));

        actual = dataDefHelper.getAxis(dataDef, AxisName.ROW);

        assertThat(actual).isEqualTo(getAxis(dataDef, "row"));
    }

    @Test
    public void testGetAxisIgnoreCase() throws FieldsException {
        DataDef dataDef = createTestDataDef();
        DAxis axis = getAxis(dataDef, "row");
        axis.setName("Row");

        DAxis actual = dataDefHelper.getAxis(dataDef, AxisName.ROW);

        assertThat(actual).isEqualTo(getAxis(dataDef, "Row"));
    }

    @Test
    public void testGetAxisFromEmptyList() throws FieldsException {
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

    private DataDef createTestDataDef() throws FieldsException {

        DMember m1 = new DMember();
        DMember m2 = new DMember();
        m2.setAxis("x");
        m1.setFields(TestUtil.createEmptyFields());
        m2.setFields(TestUtil.createEmptyFields());

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
