package net.yudichev.jiotty.connector.owntracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value.Immutable;

@Immutable
@PackagePrivateImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseOwnTracksLwt extends OwnTracksLocationUpdateOrLwt {
}
