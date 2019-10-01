package net.jiotty.connector.google.sheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import net.jiotty.common.lang.MoreThrowables;

import javax.inject.Provider;

final class NetHttpTransportProvider implements Provider<NetHttpTransport> {
    @Override
    public NetHttpTransport get() {
        return MoreThrowables.getAsUnchecked(GoogleNetHttpTransport::newTrustedTransport);
    }
}
