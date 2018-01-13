package org.codetab.gotz.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.codetab.gotz.exception.FieldsNotFoundException;
import org.codetab.gotz.exception.InvalidDataDefException;
import org.codetab.gotz.model.AxisName;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.testutil.XOBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * <p>
 * DataDefValidator tests.
 * @author Maithilish
 *
 */
public class DataDefValidatorTest {

    @Spy
    private FieldsHelper fieldsHelper;
    @InjectMocks
    private DataDefValidator validator;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /*
     * in ROW or COL axis query and script are optional and in FACT they are
     * mandatory. As coverage will show any deviation, tests only for COL and
     * FACT.
     */
    @Test
    public void testValidateQueryNullFields()
            throws JAXBException, InvalidDataDefException {

        DAxis axis = getAxis(AxisName.COL, null, null);
        axis.setFields(null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateQueryNoQueryOrScript()
            throws JAXBException, InvalidDataDefException {

        String notQuery = "<xf:xyz />";

        DAxis axis = getAxis(AxisName.COL, notQuery, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateQueryValidQuery()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:query region='abc' field='xyz' />";
        DAxis axis = getAxis(AxisName.COL, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateQueryEmptyRegion()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:query region=' ' field='xyz' />";
        DAxis axis = getAxis(AxisName.COL, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateQueryEmptyField()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:query region='abc' field=' ' />";
        DAxis axis = getAxis(AxisName.COL, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateQueryValidScript()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:script script='abc' />";
        DAxis axis = getAxis(AxisName.COL, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateQueryEmptyScript()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:script script=' ' />";
        DAxis axis = getAxis(AxisName.COL, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    /*
     * in FACT axis either query or script is mandatory
     */
    @Test
    public void testValidateFactNullFields()
            throws JAXBException, InvalidDataDefException {

        DAxis fact = getAxis(AxisName.FACT, null, null);
        fact.setFields(null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateFactNoQueryOrScript()
            throws JAXBException, InvalidDataDefException {

        String notQuery = "<xf:xyz />";

        DAxis fact = getAxis(AxisName.FACT, notQuery, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateFactValidQuery()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:query region='abc' field='xyz' />";
        DAxis fact = getAxis(AxisName.FACT, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateFactEmptyRegion()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:query region=' ' field='xyz' />";
        DAxis fact = getAxis(AxisName.FACT, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateFactEmptyField()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:query region='abc' field=' ' />";
        DAxis fact = getAxis(AxisName.FACT, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateFactValidScript()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:script script='abc' />";
        DAxis fact = getAxis(AxisName.FACT, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateFactEmptyScript()
            throws JAXBException, InvalidDataDefException {

        String query = "<xf:script script=' ' />";
        DAxis fact = getAxis(AxisName.FACT, query, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(fact);

        testRule.expect(InvalidDataDefException.class);
        validator.validate(dataDef);
    }

    @Test
    public void testValidateValidIndexRange()
            throws JAXBException, InvalidDataDefException,
            NumberFormatException, FieldsNotFoundException {

        String axisFields = "<xf:indexRange value='1-1' />";
        Fields fields = getFields(axisFields);

        DAxis axis = new DAxis();
        axis.setName("col");
        axis.setFields(fields);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();

        // check whether proper xpath (without abs path) is used
        verify(fieldsHelper).getRange("//xf:indexRange/@value", fields);
    }

    @Test
    public void testValidateIndexRangeCheckCause()
            throws JAXBException, InvalidDataDefException {

        String axisFields = "<xf:indexRange value='3-4' />";

        DAxis axis = getAxis(AxisName.COL, axisFields, null);

        DataDef dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();

        // invalid
        axisFields = "<xf:indexRange value='3-x' />";
        axis = getAxis(AxisName.COL, axisFields, null);
        dataDef.getAxis().add(axis);

        try {
            validator.validate(dataDef);
            fail("should throw InvalidDataDefException");
        } catch (InvalidDataDefException e) {
            assertThat(e.getCause()).isInstanceOf(NumberFormatException.class);
        }
    }

    @Test
    public void testValidateIndexRangeOfAllLevels()
            throws JAXBException, InvalidDataDefException {

        // invalid dataDef fields
        String dataDefFields = "<xf:indexRange value='1-x' />";
        String axisFields = "<xf:indexRange value='3-4' />";
        String memberFields = "<xf:indexRange value='5-6' />";

        DAxis axis = getAxis(AxisName.COL, axisFields, memberFields);

        DataDef dataDef = getDataDef();
        dataDef.setFields(getFields(dataDefFields));
        dataDef.getAxis().add(axis);

        try {
            validator.validate(dataDef);
            fail("should throw InvalidDataDefException");
        } catch (InvalidDataDefException e) {
        }

        // invalid axis fields
        dataDefFields = "<xf:indexRange value='1-2' />";
        axisFields = "<xf:indexRange value='3-x' />";

        dataDef.setFields(getFields(dataDefFields));
        axis = getAxis(AxisName.COL, axisFields, memberFields);

        dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        try {
            validator.validate(dataDef);
            fail("should throw InvalidDataDefException");
        } catch (InvalidDataDefException e) {
        }

        // invalid member fields
        axisFields = "<xf:indexRange value='3-4' />";
        memberFields = "<xf:indexRange value='5-x' />";

        axis = getAxis(AxisName.COL, axisFields, memberFields);

        dataDef = getDataDef();
        dataDef.getAxis().add(axis);

        try {
            validator.validate(dataDef);
            fail("should throw InvalidDataDefException");
        } catch (InvalidDataDefException e) {
        }
    }

    @Test
    public void testValidateIndexRangeOfFilter()
            throws JAXBException, InvalidDataDefException {

        String dataDefFields = "<xf:indexRange value='1-1' />";
        String axisFields = "<xf:indexRange value='3-4' />";
        String memberFields = "<xf:indexRange value='5-6' />";
        String filterFields = "<xf:indexRange value='7-8' />";

        DAxis axis = getAxis(AxisName.COL, axisFields, memberFields);

        DataDef dataDef = getDataDef();
        dataDef.setFields(getFields(dataDefFields));
        dataDef.getAxis().add(axis);

        DFilter filter = new DFilter();
        filter.setFields(getFields(filterFields));
        dataDef.getAxis().get(0).setFilter(filter);

        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();

        // invalid
        filterFields = "<xf:indexRange value='7-x' />";
        filter.setFields(getFields(filterFields));
        dataDef.getAxis().get(0).setFilter(filter);

        try {
            validator.validate(dataDef);
            fail("should throw InvalidDataDefException");
        } catch (InvalidDataDefException e) {
        }
    }

    private DataDef getDataDef() throws JAXBException {

        // @formatter:off
        List<DataDef> list = new XOBuilder<DataDef>()
        .add("<dataDef name='bs'></dataDef>")
        .build(DataDef.class);
        // @formatter:on

        return list.get(0);
    }

    private DAxis getAxis(final AxisName axisName, final String axisFields,
            final String memberFields) throws JAXBException {

        // @formatter:off
        XOBuilder<DAxis> xo = new XOBuilder<>();
        xo.add("    <axis name='" + axisName + "'>");
        if (axisFields != null) {
            xo.add("    <xf:fields>");
            xo.add(axisFields);
            xo.add("    </xf:fields>");
        }
        if (memberFields != null) {
            xo.add("  <member name='year'>");
            xo.add("    <xf:fields>");
            xo.add(memberFields);
            xo.add("    </xf:fields>");
            xo.add("  </member>");
        }
        xo.add("    </axis>");
        // @formatter:on

        return xo.build(DAxis.class).get(0);
    }

    private Fields getFields(final String fieldsContents) throws JAXBException {

        // @formatter:off
        Fields fields  = new XOBuilder<Fields>()
          .add(fieldsContents)
          .buildFields();
        // @formatter:on

        return fields;
    }
}
