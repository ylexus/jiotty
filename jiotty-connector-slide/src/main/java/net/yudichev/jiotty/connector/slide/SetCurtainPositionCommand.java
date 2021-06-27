package net.yudichev.jiotty.connector.slide;

import com.google.common.collect.ImmutableSet;
import net.yudichev.jiotty.appliance.Command;
import net.yudichev.jiotty.appliance.CommandMeta;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class SetCurtainPositionCommand implements Command<SetCurtainPositionCommand> {
    private final double position;
    private final String toStringValue;

    public SetCurtainPositionCommand(double position) {
        checkArgument(position >= 0.0 && position <= 1.0, "position must be within [0.0, 1.0] bounds");
        this.position = position;
        toStringValue = "SetCurtainPosition:" + position;
    }

    public static Set<CommandMeta<?>> allSetCurtainPositionCommandMetas() {
        return ImmutableSet.of(CommandMeta.<SetCurtainPositionCommand>builder()
                .setCommandName("POSITION")
                .putParameterTypes("pos", Double::parseDouble)
                .setCommandFactory(params -> new SetCurtainPositionCommand((Double) params.get("pos")))
                .build());
    }

    public double getPosition() {
        return position;
    }

    @Override
    public <T> Optional<T> accept(Command.Visitor<T> visitor) {
        return visitor instanceof Visitor ?
                Optional.of(((Visitor<T>) visitor).visit(this)) :
                Optional.empty();
    }

    @Override
    public String toString() {
        return toStringValue;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName") // by design
    public interface Visitor<T> extends Command.Visitor<T> {
        T visit(SetCurtainPositionCommand command);
    }
}
