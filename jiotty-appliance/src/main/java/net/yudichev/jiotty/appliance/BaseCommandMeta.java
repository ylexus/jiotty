package net.yudichev.jiotty.appliance;

import com.google.common.collect.ImmutableMap;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Map;
import java.util.function.Function;

import static org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
abstract class BaseCommandMeta<T extends Command<T>> {
    public abstract String commandName();

    @Value.Default
    public Map<String, CommandParamType> parameterTypes() {
        return ImmutableMap.of();
    }

    public abstract Function<Map<String, Object>, T> commandFactory();

    public final T createCommand(Map<String, Object> parameters) {
        return commandFactory().apply(parameters);
    }
}
