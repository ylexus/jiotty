package net.yudichev.jiotty.connector.ir.binary;

import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static net.yudichev.jiotty.common.testutil.AssertionArgumentMatcher.assertArg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BroadLinkIrDeviceTest {
    private static final byte[] PACKET_DATA = {1};
    @Mock
    private BLDevice blDevice;
    @Mock
    private BroadLinkIrDevice.DeviceSupplier deviceSupplier;

    private BroadLinkIrDevice device;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testCreatesDeviceFromFirstAttempt() throws IOException {
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", (host, mac) -> {
            assertThat(host, is("host"));
            assertThat(mac.getMacString(), is("aa:bb:cc:dd:ee:ff"));
            return blDevice;
        });
        when(blDevice.auth()).thenReturn(true);
        device.start();

        verifyDevice();
    }

    @Test
    void testRetriesThreeTimesAndThrowsIfCreationOfDeviceFails() throws IOException {
        when(deviceSupplier.create(any(), any())).thenThrow(new IOException("oops"));
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> device.start());

        assertThat(runtimeException.getMessage(), containsString("Failed"));
        //noinspection resource
        verify(deviceSupplier, times(3)).create(any(), any());
    }

    @Test
    void testStartsSuccessfullyIfCreationOfDeviceSucceedsAtLastAttempt() throws IOException {
        when(deviceSupplier.create(any(), any())).thenThrow(new IOException("oops"), new IOException("oops")).thenReturn(blDevice);
        when(blDevice.auth()).thenReturn(true);
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        device.start();

        verifyDevice();
    }

    @Test
    void testRetriesThreeTimesAndThrowsIfDeviceAuthFails() throws IOException {
        when(deviceSupplier.create(any(), any())).thenReturn(blDevice);
        when(blDevice.auth()).thenReturn(false);
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> device.start());

        assertThat(runtimeException.getMessage(), containsString("Failed"));
        verify(blDevice, times(3)).auth();
    }

    @Test
    void testStartsSuccessfullyIfDeviceAuthSucceedsAtLastAttempt() throws IOException {
        when(deviceSupplier.create(any(), any())).thenReturn(blDevice);
        when(blDevice.auth()).thenReturn(false, false, true);
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        device.start();

        verifyDevice();
    }

    @Test
    void testRetriesThreeTimesAndThrowsIfDeviceAuthThrows() throws IOException {
        when(deviceSupplier.create(any(), any())).thenReturn(blDevice);
        when(blDevice.auth()).thenThrow(new IOException("oops"));
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> device.start());

        assertThat(runtimeException.getMessage(), containsString("Failed"));
        verify(blDevice, times(3)).auth();
        verify(blDevice, times(3)).close();
    }

    @Test
    void testStartsSuccessfullyIfDeviceAuthThrowsButSucceedsAtLastAttempt() throws IOException {
        when(deviceSupplier.create(any(), any())).thenReturn(blDevice);
        when(blDevice.auth()).thenThrow(new IOException("oops1"), new IOException("oops2")).thenReturn(true);
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        device.start();

        verifyDevice();
    }

    @Test
    void whenRetryingAuthFailureClosesDevice() throws IOException {
        when(deviceSupplier.create(any(), any())).thenReturn(blDevice);
        when(blDevice.auth()).thenReturn(false, false, true);
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        device.start();

        verify(blDevice, times(2)).close();
    }

    @Test
    void whenRetryingAuthThrowingClosesDevice() throws IOException {
        when(deviceSupplier.create(any(), any())).thenReturn(blDevice);
        when(blDevice.auth()).thenThrow(new IOException("oops1"), new IOException("oops2")).thenReturn(true);
        device = new BroadLinkIrDevice("host", "aa:bb:cc:dd:ee:ff", deviceSupplier);

        device.start();

        verify(blDevice, times(2)).close();
    }

    private void verifyDevice() throws IOException {
        device.sendCommandPacket(PACKET_DATA);

        verify(blDevice).sendCmdPkt(assertArg(cmdPayload -> assertThat(((SendDataCmdPayload) cmdPayload).getData(), is(PACKET_DATA))));
    }
}