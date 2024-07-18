package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RpiDigitalPinStatusMonitorImplTest {
    @Mock
    private Context pi4jContext;
    @Mock
    private DigitalInput input;
    @Mock
    private Consumer<DigitalState> stateConsumer;
    private RpiDigitalPinStatusMonitorImpl monitor;

    @BeforeEach
    void setUp() {
        monitor = new RpiDigitalPinStatusMonitorImpl(() -> pi4jContext, 13, PullResistance.PULL_DOWN);
    }

    @Test
    void startRegistersListener() {
        doStart();

        verifyInputAddListener();
    }

    @Test
    void deliversInitialValueToNewSubscriber() {
        doStart();
        when(input.state()).thenReturn(DigitalState.HIGH);

        monitor.addListener(stateConsumer);

        verify(stateConsumer).accept(DigitalState.HIGH);
    }

    @Test
    void deliversNewValueToSubscribers() {
        doStart();
        when(input.state()).thenReturn(DigitalState.HIGH);
        DigitalStateChangeListener digitalStateChangeListener = verifyInputAddListener();
        monitor.addListener(stateConsumer);
        verify(stateConsumer).accept(DigitalState.HIGH);
        verify(stateConsumer, never()).accept(DigitalState.LOW);

        lenient().when(input.state()).thenReturn(DigitalState.LOW);
        digitalStateChangeListener.onDigitalStateChange(new DigitalStateChangeEvent<>(input, DigitalState.LOW));

        verify(stateConsumer).accept(DigitalState.LOW);
    }

    @Test
    void stopsDeliveringNewValuesToUnsubscribedSubscribers() {
        doStart();
        when(input.state()).thenReturn(DigitalState.HIGH);
        monitor.addListener(stateConsumer).close();
        DigitalStateChangeListener digitalStateChangeListener = verifyInputAddListener();

        digitalStateChangeListener.onDigitalStateChange(new DigitalStateChangeEvent<>(input, DigitalState.LOW));

        verify(stateConsumer).accept(DigitalState.HIGH);
        verifyNoMoreInteractions(stateConsumer);
    }

    @Test
    void stopRemovesControllerListener() {
        doStart();
        DigitalStateChangeListener listener = verifyInputAddListener();

        monitor.stop();

        verify(input).shutdown(pi4jContext);
    }

    private DigitalStateChangeListener verifyInputAddListener() {
        ArgumentCaptor<DigitalStateChangeListener> listenerCaptor = ArgumentCaptor.forClass(DigitalStateChangeListener.class);
        verify(input).addListener(listenerCaptor.capture());
        return listenerCaptor.getValue();
    }

    private void doStart() {
        var digitalInputProvider = mock(DigitalInputProvider.class);
        when(pi4jContext.provider("gpiod-digital-input")).thenReturn(digitalInputProvider);
        when(digitalInputProvider.create(any(DigitalInputConfig.class))).thenReturn(input);
        monitor.start();
        verify(digitalInputProvider).create(ArgumentMatchers.<DigitalInputConfig>assertArg(digitalInputConfig -> {
            assertThat(digitalInputConfig.address(), is(13));
            assertThat(digitalInputConfig.pull(), is(PullResistance.PULL_DOWN));
        }));
    }
}