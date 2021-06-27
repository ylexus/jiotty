package net.yudichev.jiotty.appliance;

import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public final class EnumCommands {
    private EnumCommands() {
    }

    public static <T extends Enum<T> & Command<T>> Set<CommandMeta<?>> createMetasForSimpleEnumCommand(T[] allEnumValues) {
        return Stream.of(allEnumValues)
                .map(enumCommand -> CommandMeta.<T>builder()
                        .setCommandName(enumCommand.name())
                        .setCommandFactory(parameters -> enumCommand)
                        .build())
                .collect(toImmutableSet());
    }
}
