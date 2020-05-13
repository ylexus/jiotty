package net.yudichev.jiotty.connector.slide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;

import static org.immutables.value.Value.Immutable;

@Immutable
@PackagePrivateImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseAuthenticationResponse {
    @JsonProperty("access_token")
    String accessToken();

    @JsonProperty("expires_in")
    long expiresInSeconds();
}
