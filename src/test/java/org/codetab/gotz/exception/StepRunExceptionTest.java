package org.codetab.gotz.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class StepRunExceptionTest {

    private StepRunException exception;

    @Test
    public void testStepRunException() {
        String message = "xyz";
        exception = new StepRunException(message);
        String actual = exception.getMessage();

        String expected = "[" + message + "]";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testStepRunExceptionWithCause() {
        Throwable cause = new Throwable("x");
        exception = new StepRunException(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    public void testStepRunExceptionWithMessageAndCause() {
        Throwable cause = new Throwable("x");
        String message = "xyz";
        exception = new StepRunException(message, cause);

        String expected = "[" + message + "]";

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo(expected);
    }

}
