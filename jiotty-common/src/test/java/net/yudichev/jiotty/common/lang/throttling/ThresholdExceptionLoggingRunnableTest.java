package net.yudichev.jiotty.common.lang.throttling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdExceptionLoggingRunnableTest {
    @Mock
    private Runnable delegate;
    @SuppressWarnings("NonConstantLogger")
    @Mock
    private Logger logger;
    private Runnable runnable;

    @BeforeEach
    void setUp() {
        runnable = ThresholdExceptionLoggingRunnable.withExceptionLoggedAfterThreshold(logger, "description", 2, delegate);
    }

    @Test
    void logsSubsequentErrorsOnlyAfterThreshold() {
        RuntimeException exception = new RuntimeException("oops");
        doThrow(exception).when(delegate).run();

        runnable.run();
        verifyZeroInteractions(logger);

        runnable.run();
        verifyZeroInteractions(logger);

        runnable.run();
        verify(logger).error(any(String.class), contains("description"), eq(exception));
    }

    @Test
    void resetsErrorCounterIfNoException() {
        RuntimeException exception = new RuntimeException("oops");
        doThrow(exception)
                .doThrow(exception)
                .doNothing()
                .doThrow(exception)
                .when(delegate).run();

        runnable.run();
        runnable.run();
        runnable.run();
        runnable.run();
        verifyZeroInteractions(logger);
    }
}