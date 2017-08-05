package org.codetab.gotz.model.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDeepCloneFieldsBase() {
        FieldsHelper fieldsHelper = new FieldsHelper();

        Field field = TestUtil.createField("f", "v");
        Fields fields = TestUtil.createFields("x", "y", field);

        FieldsBase actual = fieldsHelper.deepClone(fields);

        assertThat(actual).isEqualTo(fields);
        assertThat(actual).isNotSameAs(fields);
    }

    @Test
    public void testDeepCloneListOfFieldsBase() {
        FieldsHelper fieldsHelper = new FieldsHelper();

        Field field = TestUtil.createField("f", "v");
        List<FieldsBase> fields = TestUtil.asList(field);

        List<FieldsBase> actual = fieldsHelper.deepClone(fields);

        assertThat(actual).isEqualTo(fields);
        assertThat(actual).isNotSameAs(fields);
    }

}
