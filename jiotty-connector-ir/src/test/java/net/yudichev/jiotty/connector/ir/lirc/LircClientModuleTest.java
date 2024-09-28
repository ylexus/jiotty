package net.yudichev.jiotty.connector.ir.lirc;

import com.google.inject.Guice;
import com.google.inject.Key;
import net.yudichev.jiotty.common.async.ExecutorModule;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LircClientModuleTest {
    @Test
    void injector() {
        var module = LircClientModule.builder().build();
        assertThat(module.getExposedKey(), is(Key.get(LircClient.class)));
        Guice.createInjector(new ExecutorModule(), module).getBinding(module.getExposedKey());
    }
}