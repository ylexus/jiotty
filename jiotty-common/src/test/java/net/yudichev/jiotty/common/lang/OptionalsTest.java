package net.yudichev.jiotty.common.lang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OptionalsTest {
    @Mock
    private Consumer<Integer> presentValueConsumer;
    @Mock
    private Runnable emptyValueAction;

    @Test
    void ifPresentOrElseWithPresentValue() {
        Optionals.ifPresent(Optional.of(42), presentValueConsumer).orElse(emptyValueAction);
        verify(presentValueConsumer).accept(42);
        verifyNoInteractions(emptyValueAction);
    }

    @Test
    void ifPresentOrElseWithEmptyValue() {
        Optionals.ifPresent(Optional.empty(), presentValueConsumer).orElse(emptyValueAction);
        verify(emptyValueAction).run();
        verifyNoInteractions(presentValueConsumer);
    }
}