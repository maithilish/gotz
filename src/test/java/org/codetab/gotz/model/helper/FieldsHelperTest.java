package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import org.codetab.gotz.model.Field;
import org.codetab.gotz.model.Fields;
import org.codetab.gotz.model.FieldsBase;
import org.codetab.gotz.testutil.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * <p>
 * FieldsHelper tests.
 * @author Maithilish
 *
 */
public class FieldsHelperTest {

    private FieldsHelper fieldsHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        fieldsHelper = new FieldsHelper();
    }

    @Test
    public void testDeepCloneFieldsBase() {

        Field field = TestUtil.createField("f", "v");
        Fields fields = TestUtil.createFields("x", "y", field);

        FieldsBase actual = fieldsHelper.deepClone(fields);

        assertThat(actual).isEqualTo(fields);
        assertThat(actual).isNotSameAs(fields);
    }

    @Test
    public void testDeepCloneFieldsBaseNullParams()
            throws DataFormatException, IOException {
        try {
            Fields fields = null;
            fieldsHelper.deepClone(fields);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fieldsBase must not be null");
        }
    }

    @Test
    public void testDeepCloneListOfFieldsBase() {

        Field field = TestUtil.createField("f", "v");
        List<FieldsBase> fields = TestUtil.asList(field);

        List<FieldsBase> actual = fieldsHelper.deepClone(fields);

        assertThat(actual).isEqualTo(fields);
        assertThat(actual).isNotSameAs(fields);
    }

    @Test
    public void testDeepCloneListOfFieldsNullParams()
            throws DataFormatException, IOException {
        try {
            List<FieldsBase> fields = null;
            fieldsHelper.deepClone(fields);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fields must not be null");
        }
    }
}
