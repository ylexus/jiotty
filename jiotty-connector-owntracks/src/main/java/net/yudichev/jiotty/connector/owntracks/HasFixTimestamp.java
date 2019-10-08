package net.yudichev.jiotty.connector.owntracks;

import com.fasterxml.jackson.annotation.JsonProperty;

interface HasFixTimestamp {
    @JsonProperty("tst")
    long fixTimestampSeconds();
}
