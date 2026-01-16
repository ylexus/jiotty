package net.yudichev.jiotty.connector.ir.binary;

public interface IrDevice {
    /// @param packetData binary IR packet data.
    /// @deprecated use [#sendCommandPacket(byte[])]
    @Deprecated
    default void sendCmdPkt(byte[] packetData) {
        sendCommandPacket(packetData);
    }

    void sendCommandPacket(byte[] packetData);
}
