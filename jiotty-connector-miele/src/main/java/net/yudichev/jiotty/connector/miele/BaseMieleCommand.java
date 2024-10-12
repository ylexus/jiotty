package net.yudichev.jiotty.connector.miele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.OptionalInt;

@Value.Immutable
@PublicImmutablesStyle
@JsonSerialize
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseMieleCommand {
    OptionalInt programId();

    Optional<Boolean> powerOn();

    Optional<Boolean> powerOff();
}
