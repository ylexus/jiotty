package net.yudichev.jiotty.connector.miele;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.function.Consumer;

interface OAuth2TokenManager {
    Closeable subscribeToAccessToken(Consumer<? super String> accessTokenHandler);
}
