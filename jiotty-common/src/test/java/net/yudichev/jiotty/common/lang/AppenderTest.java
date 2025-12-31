package net.yudichev.jiotty.common.lang;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AppenderTest {
    @Test
    void iterable() {
        var sb = new StringBuilder();
        Appender appender = Appender.wrap(sb);
        appender.append(List.of(1, 2, 3), (a, object) -> a.append(object).append('+'))
                .append(List.of(), Appender::append)
                .append(List.of(4, 5));
        assertThat(sb.toString()).isEqualTo("[1+, 2+, 3+][][4, 5]");
    }
}