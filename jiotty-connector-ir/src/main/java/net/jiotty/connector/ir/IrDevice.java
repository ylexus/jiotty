package net.jiotty.connector.ir;

public interface IrDevice {
    void sendCmdPkt(byte[] packetData);
}
