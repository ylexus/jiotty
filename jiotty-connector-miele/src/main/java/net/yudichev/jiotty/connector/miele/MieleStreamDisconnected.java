package net.yudichev.jiotty.connector.miele;

public final class MieleStreamDisconnected implements MieleEvent {
    public static final MieleStreamDisconnected STREAM_DISCONNECTED = new MieleStreamDisconnected();

    private MieleStreamDisconnected() {
    }

    @Override
    public String toString() {
        return "STREAM_DISCONNECTED";
    }
}
