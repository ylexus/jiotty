package net.yudichev.jiotty.common.lang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EitherTest {
    @Test
    void mapsLeft() {
        assertThat(Either.<String, String>left("Value").<String>map(String::toUpperCase, String::toLowerCase)).isEqualTo("VALUE");
    }

    @Test
    void mapsRight() {
        assertThat(Either.<String, String>right("Value").<String>map(String::toUpperCase, String::toLowerCase)).isEqualTo("value");
    }

    @Test
    void mapsLeftNull() {
        assertThat(Either.<String, String>left(null).<String>map(s -> "left", s -> "right")).isEqualTo("left");
    }

    @Test
    void mapsRightNull() {
        assertThat(Either.<String, String>right(null).<String>map(s -> "left", s -> "right")).isEqualTo("right");
    }

    @Test
    void mapLeft() {
        assertThat(Either.<String, String>left("left").mapLeft(String::toUpperCase)).isEqualTo(Either.left("LEFT"));
        assertThat(Either.<String, String>right("right").mapLeft(String::toUpperCase)).isEqualTo(Either.right("right"));
    }

    @Test
    void mapRight() {
        assertThat(Either.<String, String>left("left").mapRight(String::toUpperCase)).isEqualTo(Either.left("left"));
        assertThat(Either.<String, String>right("right").mapRight(String::toUpperCase)).isEqualTo(Either.right("RIGHT"));
    }
}