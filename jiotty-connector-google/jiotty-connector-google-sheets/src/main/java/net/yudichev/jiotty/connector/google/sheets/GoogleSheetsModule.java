package net.yudichev.jiotty.connector.google.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import net.yudichev.jiotty.connector.google.common.impl.BaseGoogleServiceModule;

public final class GoogleSheetsModule extends BaseGoogleServiceModule implements ExposedKeyModule<GoogleSheetsClient> {
    private GoogleSheetsModule(BindingSpec<GoogleAuthorization> googleAuthorizationSpec) {
        super(googleAuthorizationSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(Sheets.class).annotatedWith(Bindings.Internal.class).toProvider(SheetsProvider.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
                        .implement(GoogleSpreadsheet.class, InternalGoogleSpreadsheet.class)
                        .build(GoogleSpreadsheetFactory.class));
        bind(getExposedKey()).to(GoogleSheetsClientImpl.class);
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<GoogleSheetsClient, Builder> {
        @Override
        public ExposedKeyModule<GoogleSheetsClient> build() {
            return new GoogleSheetsModule(getAuthorizationSpec());
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
