package net.yudichev.jiotty.common.lang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvenMoreObjectsTest {
    @Test
    void mapIfNotNull() {
        assertThat(EvenMoreObjects.<String, Integer>mapIfNotNull(null, Integer::parseInt)).isNull();
        assertThat(EvenMoreObjects.<String, Integer>mapIfNotNull("13", Integer::parseInt)).isEqualTo(13);
    }
}