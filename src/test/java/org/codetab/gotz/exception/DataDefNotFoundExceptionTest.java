package org.codetab.gotz.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class DataDefNotFoundExceptionTest {

    private DataDefNotFoundException exception;

    @Test
    public void testDataDefNotFoundException() {
        String message = "xyz";
        exception = new DataDefNotFoundException(message);
        String actual = exception.getMessage();

        String expected = "[" + message + "]";

        assertThat(actual).isEqualTo(expected);
    }

}
