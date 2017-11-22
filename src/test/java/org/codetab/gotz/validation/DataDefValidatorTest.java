package org.codetab.gotz.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.codetab.gotz.exception.XFieldException;
import org.codetab.gotz.model.DAxis;
import org.codetab.gotz.model.DFilter;
import org.codetab.gotz.model.DMember;
import org.codetab.gotz.model.DataDef;
import org.codetab.gotz.model.XField;
import org.codetab.gotz.model.helper.XFieldHelper;
import org.codetab.gotz.testutil.TestUtil;
import org.codetab.gotz.testutil.XFieldBuilder;
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
    private XFieldHelper xFieldHelper;
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
    public void testValidateDataDefField() throws XFieldException {
        XField xField = TestUtil.createXField("indexRange", "1-1");
        DataDef dataDef = new DataDef();
        dataDef.setXfield(xField);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateAxisField() throws XFieldException {
        XField xField = TestUtil.createXField("indexRange", "1-1");
        DAxis axis = new DAxis();
        axis.setXfield(xField);

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(axis);

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateMemberField() throws XFieldException {
        XField xField = TestUtil.createXField("indexRange", "1-1");

        DMember member = new DMember();
        member.setXfield(xField);

        DAxis axis = new DAxis();
        axis.getMember().add(member);
        axis.setXfield(new XField());

        DataDef dataDef = new DataDef();
        dataDef.getAxis().add(axis);
        dataDef.setXfield(new XField());

        boolean actual = validator.validate(dataDef);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateInvalidRange() throws XFieldException {
        XField dataDefField =
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf");
        XField axisField =
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf");
        XField memberField =
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf");
        XField filterField =
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf");

        DMember member = new DMember();
        member.setXfield(memberField);

        DFilter filter = new DFilter();
        filter.setXfield(filterField);

        DAxis axis = new DAxis();
        axis.setXfield(axisField);
        axis.getMember().add(member);
        axis.setFilter(filter);

        DataDef dataDef = new DataDef();
        dataDef.setXfield(dataDefField);
        dataDef.getAxis().add(axis);

        assertThat(validator.validate(dataDef)).isTrue();

        // invalid range
        filter.setXfield(new XFieldBuilder()
                .add("<xf:indexRange value='1-x' />").build("xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        filter.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf"));
        member.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-x' />", "xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        axis.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-x' />", "xf"));
        member.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        // invalid range
        axis.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf"));
        dataDef.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-x' />", "xf"));
        assertThat(validator.validate(dataDef)).isFalse();

        dataDef.setXfield(
                TestUtil.buildXField("<xf:indexRange value='1-1' />", "xf"));
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
