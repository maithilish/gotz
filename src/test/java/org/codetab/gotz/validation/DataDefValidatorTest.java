package org.codetab.gotz.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.codetab.gotz.exception.FieldsException;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.helper.FieldsHelper;
import org.codetab.gotz.testutil.FieldsBuilder;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidateNoField() {
        DataDef dataDef = new DataDef();
        boolean actual = validator.validate(dataDef);
        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateDataDefField() throws FieldsException {
        Fields fields = TestUtil.createFields("indexRange", "1-1");
        DataDef dataDef = new DataDef();
        dataDef.setFields(fields);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateAxisField() throws FieldsException {
        Fields fields = TestUtil.createFields("indexRange", "1-1");
        DAxis axis = new DAxis();
        axis.setFields(fields);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateMemberField() throws FieldsException {
        Fields fields = TestUtil.createFields("indexRange", "1-1");

        DMember member = new DMember();
        member.setFields(fields);

        DAxis axis = new DAxis();
        axis.getMember().add(member);
        axis.setFields(new Fields());

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(axis);
        dataDef.setFields(new Fields());

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateInvalidRange() throws FieldsException {
        Fields dataDefField =
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf");
        Fields axisField =
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf");
        Fields memberField =
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf");
        Fields filterField =
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf");

        DMember member = new DMember();
        member.setFields(memberField);

        DFilter filter = new DFilter();
        filter.setFields(filterField);

        DAxis axis = new DAxis();
        axis.setFields(axisField);
        axis.getMember().add(member);
        axis.setFilter(filter);

        DataDef dataDef = new DataDef();
        dataDef.setFields(dataDefField);
        dataDef.getAxis().add(axis);

        assertThat(validator.validate(dataDef)).isTrue();

        // invalid range
        filter.setFields(new FieldsBuilder()
                .add("<xf:indexRange value='1-x' />").build("xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        filter.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf"));
        member.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-x' />", "xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        axis.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-x' />", "xf"));
        member.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        axis.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf"));
        dataDef.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-x' />", "xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        dataDef.setFields(
                TestUtil.buildFields("<xf:indexRange value='1-1' />", "xf"));
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
