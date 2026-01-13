package net.yudichev.jiotty.common.lang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
abstract class DeduplicatingConsumerTest {
    @Mock
    private Consumer<String> delegate;
    private Consumer<String> deduplicatingConsumer;

    @BeforeEach
    void setUp() {
        deduplicatingConsumer = createInstance(EqualityComparator.equality(), delegate);
    }

    protected abstract Consumer<String> createInstance(EqualityComparator<String> equalityComparator, Consumer<String> delegate);

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void propagatesInitialValue() {
        deduplicatingConsumer.accept("Potter");
        verify(delegate).accept("Potter");
    }

    @Test
    void doesNotPropagateDuplicate() {
        deduplicatingConsumer.accept("Potter");
        deduplicatingConsumer.accept("Potter");
        verify(delegate).accept("Potter");
    }

    @Test
    void doesPropagateDifferentValue() {
        deduplicatingConsumer.accept("Potter");
        verify(delegate).accept("Potter");
        deduplicatingConsumer.accept("Harry");
        verify(delegate).accept("Harry");
    }
}