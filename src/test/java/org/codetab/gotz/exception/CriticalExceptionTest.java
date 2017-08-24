package org.codetab.gotz.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class CriticalExceptionTest {

    private CriticalException exception;

    @Test
    public void testCriticalException() {
        String message = "xyz";
        exception = new CriticalException(message);
        String actual = exception.getMessage();

        String expected = "[" + message + "]";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testCriticalExceptionWithCause() {
        Throwable cause = new Throwable("x");
        exception = new CriticalException(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    public void testCriticalExceptionWithMessageAndCause() {
        Throwable cause = new Throwable("x");
        String message = "xyz";
        exception = new CriticalException(message, cause);

        String expected = "[" + message + "]";

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo(expected);
    }

}
