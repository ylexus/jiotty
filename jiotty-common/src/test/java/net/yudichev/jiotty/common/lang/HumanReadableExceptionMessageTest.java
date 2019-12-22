package net.yudichev.jiotty.common.lang;

import org.junit.jupiter.api.Test;

import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HumanReadableExceptionMessageTest {
    @Test
    void singleException() {
        assertThat(humanReadableMessage(new RuntimeException("msg")), is("msg"));
    }

    @Test
    void nestedWithNoParentMessage() {
        assertThat(humanReadableMessage(new RuntimeException(new RuntimeException("msg"))), is("msg"));
    }

    @Test
    void nestedWithParentMessage() {
        assertThat(humanReadableMessage(new RuntimeException("parent", new RuntimeException("msg"))), is("parent: msg"));
    }

    @SuppressWarnings("NewExceptionWithoutArguments")
    @Test
    void interruptedException() {
        assertThat(humanReadableMessage(new RuntimeException("msg", new InterruptedException())), is("msg: Interrupted"));
    }
}