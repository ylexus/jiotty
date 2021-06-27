package net.yudichev.jiotty.appliance;

import java.util.Optional;
import java.util.Set;

import static net.yudichev.jiotty.appliance.EnumCommands.createMetasForSimpleEnumCommand;

public enum PowerCommand implements Command<PowerCommand> {
    ON,
    OFF;

    private static final Set<CommandMeta<?>> ALL_METAS = createMetasForSimpleEnumCommand(values());

    public static Set<CommandMeta<?>> allPowerCommandMetas() {
        return ALL_METAS;
    }

    @Override
    public <T> Optional<T> accept(Command.Visitor<T> visitor) {
        return visitor instanceof Visitor ?
                Optional.of(((Visitor<T>) visitor).visit(this)) :
                Optional.empty();
    }

    @SuppressWarnings("ClassNameSameAsAncestorName") // by design
    public interface Visitor<T> extends Command.Visitor<T> {
        T visit(PowerCommand command);
    }
}
