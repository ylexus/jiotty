package net.yudichev.jiotty.connector.ir;

public interface IrDevice {
    /**
     * @param packetData binary IR packet data.
     * @deprecated use {@link #sendCommandPacket(byte[])}
     */
    @Deprecated
    default void sendCmdPkt(byte[] packetData) {
        sendCommandPacket(packetData);
    }

    void sendCommandPacket(byte[] packetData);
}
