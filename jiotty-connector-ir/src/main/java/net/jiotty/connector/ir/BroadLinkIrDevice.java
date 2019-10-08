package net.jiotty.connector.ir;

import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;
import com.google.inject.BindingAnnotation;
import net.jiotty.common.inject.BaseLifecycleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.jiotty.common.lang.MoreThrowables.asUnchecked;

final class BroadLinkIrDevice extends BaseLifecycleComponent implements IrDevice {
    private static final Logger logger = LoggerFactory.getLogger(BroadLinkIrDevice.class);
    private final String host;
    private final String macAddress;
    private final Object lock = new Object();
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") // IDEA inspection failure
    private RM2Device device;

    @Inject
    BroadLinkIrDevice(@Host String host, @MacAddress String macAddress) {
        this.host = checkNotNull(host);
        this.macAddress = checkNotNull(macAddress);
    }

    @Override
    public void doStart() {
        synchronized (lock) {
            try {
                device = new RM2Device(host, new Mac(macAddress));
                checkState(device.auth(), "Unable to authenticate BroadLink device on host %s, MAC %s", host, macAddress);
                logger.info("RM2 Device ready at {}: {}", device.getHost(), device.getDeviceDescription());
            } catch (IOException e) {
                throw new RuntimeException("Unable to initialize Broadlink device", e);
            }
        }
    }

    @Override
    public void sendCmdPkt(byte[] packetData) {
        synchronized (lock) {
            asUnchecked(() -> device.sendCmdPkt(new SendDataCmdPayload(packetData)));
        }
    }

    @Override
    protected void doStop() {
        synchronized (lock) {
            device.close();
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Host {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface MacAddress {
    }
}
