package net.jiotty.connector.google.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.jiotty.common.inject.ExposedKeyModule;
import net.jiotty.connector.google.GoogleApiSettings;
import net.jiotty.connector.google.impl.BaseGoogleServiceModule;

import javax.inject.Singleton;

public final class GoogleSheetsModule extends BaseGoogleServiceModule implements ExposedKeyModule<GoogleSheetsClient> {
    private GoogleSheetsModule(GoogleApiSettings settings) {
        super(settings);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(NetHttpTransport.class).toProvider(NetHttpTransportProvider.class).in(Singleton.class);
        bind(Credential.class).toProvider(CredentialProvider.class).in(Singleton.class);

        bind(Sheets.class).annotatedWith(Bindings.Internal.class).toProvider(SheetsProvider.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
                .implement(GoogleSpreadsheet.class, InternalGoogleSpreadsheet.class)
                .build(GoogleSpreadsheetFactory.class));
        bind(getExposedKey()).to(GoogleSheetsClientImpl.class);
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<ExposedKeyModule<GoogleSheetsClient>, Builder> {
        @Override
        public ExposedKeyModule<GoogleSheetsClient> build() {
            return new GoogleSheetsModule(getSettings());
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
