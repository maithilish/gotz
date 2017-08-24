package org.codetab.gotz.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class FieldNotFoundExceptionTest {

    private FieldNotFoundException exception;

    @Test
    public void testFieldNotFoundException() {
        String message = "xyz";
        exception = new FieldNotFoundException(message);
        String actual = exception.getMessage();

        String expected = "[" + message + "]";

        assertThat(actual).isEqualTo(expected);
    }

}
