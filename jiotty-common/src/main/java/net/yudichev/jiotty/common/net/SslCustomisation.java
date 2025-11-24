package net.yudichev.jiotty.common.net;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

public record SslCustomisation(SSLSocketFactory socketFactory, X509TrustManager trustManager) {
    public static SslCustomisation fromTrustStore(InputStream trustStoreStream, String trustStorePassword) {
        SSLContext sslContext = null;
        try {
            KeyStore ts = KeyStore.getInstance("PKCS12");
            ts.load(trustStoreStream, trustStorePassword.toCharArray());

            var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            sslContext = SSLContext.getInstance("TLS");
            var trustManagers = tmf.getTrustManagers();
            sslContext.init(null, trustManagers, null);

            if (trustManagers.length == 1 && trustManagers[0] instanceof X509TrustManager x509TrustManager) {
                return new SslCustomisation(sslContext.getSocketFactory(), x509TrustManager);
            } else {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
        } catch (KeyStoreException | KeyManagementException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed initialising SSL context", e);
        }
    }
}
