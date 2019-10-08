package net.yudichev.jiotty.appliance;

import java.util.Optional;

public enum PowerCommand implements Command {
    ON,
    OFF;

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
