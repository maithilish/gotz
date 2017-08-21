package org.codetab.gotz.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Field;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * DataDefValidator tests.
 * @author Maithilish
 *
 */
public class DataDefValidatorTest {

    private DataDefValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new DataDefValidator();
    }

    @Test
    public void testValidateNoField() {
        DataDef dataDef = new DataDef();
        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateDataDefField() {
        Field field = TestUtil.createField("indexRange", "1-1");
        DataDef dataDef = new DataDef();
        dataDef.getFields().add(field);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateAxisField() {
        Field field = TestUtil.createField("indexRange", "1-1");
        DAxis axis = new DAxis();
        axis.getFields().add(field);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateMemberField() {
        Field field = TestUtil.createField("indexRange", "1-1");

        DMember member = new DMember();
        member.getFields().add(field);

        DAxis axis = new DAxis();
        axis.getMember().add(member);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateInvalidRange() {
        Field dataDefField = TestUtil.createField("indexRange", "1-1");
        Field axisField = TestUtil.createField("indexRange", "1-1");
        Field memberField = TestUtil.createField("indexRange", "1-1");
        Field filterField = TestUtil.createField("indexRange", "1-1");

        DMember member = new DMember();
        member.getFields().add(memberField);

        DFilter filter = new DFilter();
        filter.getFields().add(filterField);

        DAxis axis = new DAxis();
        axis.getFields().add(axisField);
        axis.getMember().add(member);
        axis.setFilter(filter);

        DataDef dataDef = new DataDef();
        dataDef.getFields().add(dataDefField);
        dataDef.getAxis().add(axis);

        assertThat(validator.validate(dataDef)).isTrue();

        // invalid range
        filterField.setValue("1-x");
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        filterField.setValue("1-1");
        memberField.setValue("1-x");
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        memberField.setValue("1-1");
        axisField.setValue("1-x");
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        axisField.setValue("1-1");
        dataDefField.setValue("1-x");
        assertThat(validator.validate(dataDef)).isFalse();

        dataDefField.setValue("1-1");
        assertThat(validator.validate(dataDef)).isTrue();

    }

    @Test
    public void testValidateNullParams() {
        try {
            validator.validate(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }
}
