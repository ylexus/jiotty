package net.yudichev.jiotty.common.security;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.function.Consumer;

public interface OAuth2TokenManager {
    Closeable subscribeToAccessToken(Consumer<? super String> accessTokenHandler);
}
