package net.yudichev.jiotty.connector.ir;

public interface IrDevice {
    void sendCmdPkt(byte[] packetData);
}
