package org.codetab.gotz.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class ConfigNotFoundExceptionTest {

    private ConfigNotFoundException exception;

    @Test
    public void testConfigNotFoundException() {
        String message = "xyz";
        exception = new ConfigNotFoundException(message);
        String actual = exception.getMessage();

        String expected = "[" + message + "]";

        assertThat(actual).isEqualTo(expected);
    }

}
