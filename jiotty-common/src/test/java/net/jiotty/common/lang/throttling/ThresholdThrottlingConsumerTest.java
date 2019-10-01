package net.jiotty.common.lang.throttling;

import net.jiotty.common.time.CurrentDateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdThrottlingConsumerTest {
    @Mock
    private CurrentDateTimeProvider currentDateTimeProvider;
    @Mock
    private Consumer<String> delegate;
    private ThresholdThrottlingConsumer<String> throttlingConsumer;

    @BeforeEach
    void setUp() {
        throttlingConsumer = new ThresholdThrottlingConsumer<>(currentDateTimeProvider, 2, Duration.ofSeconds(5), delegate);
    }

    @Test
    void logsOnlyOneAtThreshold() {
        timeNowSeconds(1);

        throttlingConsumer.accept("one");
        throttlingConsumer.accept("two");
        throttlingConsumer.accept("three");
        verify(delegate).accept("three");
        throttlingConsumer.accept("four");
        throttlingConsumer.accept("five");
        throttlingConsumer.accept("six");
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void logsAgainAfterTimeout() {
        logsOnlyOneAtThreshold();

        timeNowSeconds(7);
        throttlingConsumer.accept("seven");
        throttlingConsumer.accept("eight");
        throttlingConsumer.accept("nine");

        verify(delegate).accept("nine");
        verifyNoMoreInteractions(delegate);
    }

    private void timeNowSeconds(int epochSecond) {
        when(currentDateTimeProvider.currentInstant()).thenReturn(Instant.ofEpochSecond(epochSecond));
    }
}