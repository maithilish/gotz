package org.codetab.gotz.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class StepPersistenceExceptionTest {

    private StepPersistenceException exception;

    @Test
    public void testStepPersistenceException() {
        String message = "xyz";
        exception = new StepPersistenceException(message);
        String actual = exception.getMessage();

        String expected = "[" + message + "]";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testStepPersistenceExceptionWithCause() {
        Throwable cause = new Throwable("x");
        exception = new StepPersistenceException(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    public void testStepPersistenceExceptionWithMessageAndCause() {
        Throwable cause = new Throwable("x");
        String message = "xyz";
        exception = new StepPersistenceException(message, cause);

        String expected = "[" + message + "]";

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo(expected);
    }

}
