package net.jiotty.common.inject;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class GuiceUtilTest {
    @Test
    void uniqueAnnotation() {
        assertThat(GuiceUtil.uniqueAnnotation(), is(not(GuiceUtil.uniqueAnnotation())));
    }
}