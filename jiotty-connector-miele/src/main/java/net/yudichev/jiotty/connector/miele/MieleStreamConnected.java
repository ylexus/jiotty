package net.yudichev.jiotty.connector.miele;

public final class MieleStreamConnected implements MieleEvent {
    public static final MieleStreamConnected STREAM_CONNECTED = new MieleStreamConnected();

    private MieleStreamConnected() {
    }

    @Override
    public String toString() {
        return "STREAM_CONNECTED";
    }
}
