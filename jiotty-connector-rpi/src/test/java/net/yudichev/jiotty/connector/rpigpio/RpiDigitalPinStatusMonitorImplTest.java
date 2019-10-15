package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RpiDigitalPinStatusMonitorImplTest {
    @Mock
    private GpioController gpioController;
    @Mock
    private GpioPinDigitalInput input;
    @Mock
    private Consumer<PinState> stateConsumer;
    private RpiDigitalPinStatusMonitorImpl monitor;

    @BeforeEach
    void setUp() {
        monitor = new RpiDigitalPinStatusMonitorImpl(() -> gpioController, PiFacePin.GPIO_13, PinPullResistance.PULL_DOWN);
    }

    @Test
    void startProvisionsPin() {
        doStart();

        verify(input).setShutdownOptions(true);
    }

    @Test
    void startRegistersListener() {
        doStart();

        verifyInputAddListener();
    }

    @Test
    void deliversInitialValueToNewSubscriber() {
        doStart();
        when(input.getState()).thenReturn(PinState.HIGH);

        monitor.addListener(stateConsumer);

        verify(stateConsumer).accept(PinState.HIGH);
    }

    @Test
    void deliversNewValueToSubscribers() {
        doStart();
        when(input.getState()).thenReturn(PinState.HIGH);
        GpioPinListenerDigital gpioPinListener = verifyInputAddListener();
        monitor.addListener(stateConsumer);
        verify(stateConsumer).accept(PinState.HIGH);
        verify(stateConsumer, never()).accept(PinState.LOW);

        gpioPinListener.handleGpioPinDigitalStateChangeEvent(new GpioPinDigitalStateChangeEvent(new Object(), input, PinState.LOW));

        verify(stateConsumer).accept(PinState.LOW);
    }

    @Test
    void stopsDeliveringNewValuesToUnsubscribedSubscribers() {
        doStart();
        when(input.getState()).thenReturn(PinState.HIGH);
        monitor.addListener(stateConsumer).close();
        GpioPinListenerDigital gpioPinListener = verifyInputAddListener();

        gpioPinListener.handleGpioPinDigitalStateChangeEvent(new GpioPinDigitalStateChangeEvent(new Object(), input, PinState.LOW));

        verify(stateConsumer).accept(PinState.HIGH);
        verifyNoMoreInteractions(stateConsumer);
    }

    @Test
    void stopRemovesControllerListener() {
        doStart();
        GpioPinListenerDigital listener = verifyInputAddListener();

        monitor.stop();

        verify(input).removeListener(listener);
    }

    private GpioPinListenerDigital verifyInputAddListener() {
        ArgumentCaptor<GpioPinListenerDigital> listenerCaptor = ArgumentCaptor.forClass(GpioPinListenerDigital.class);
        verify(input).addListener(listenerCaptor.capture());
        return listenerCaptor.getValue();
    }

    private void doStart() {
        when(gpioController.provisionDigitalInputPin(PiFacePin.GPIO_13, PinPullResistance.PULL_DOWN)).thenReturn(input);
        monitor.start();
    }
}