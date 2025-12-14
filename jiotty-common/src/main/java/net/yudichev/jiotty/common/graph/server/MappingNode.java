package net.yudichev.jiotty.common.graph.server;

import jakarta.annotation.Nullable;
import net.yudichev.jiotty.common.graph.Node;

import java.util.Objects;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MappingNode<TN extends BaseServerNode, SN extends Node, U> extends BaseServerNode {
    private final String valueName;
    private final SN sourceNode;
    private final Function<SN, U> mapper;
    private final ChangeDetector<TN, U> changeDetector;

    @Nullable
    private U value;

    protected MappingNode(GraphRunner runner, String valueName, SN sourceNode, Function<SN, U> mapper) {
        this(runner, valueName, sourceNode, mapper, (thisNode, oldValue, newValue) -> !Objects.equals(oldValue, newValue));
    }

    protected MappingNode(GraphRunner runner, String valueName, SN sourceNode, Function<SN, U> mapper, ChangeDetector<TN, U> changeDetector) {
        super(runner);
        this.valueName = checkNotNull(valueName);
        this.sourceNode = subscribeTo(sourceNode);
        this.mapper = checkNotNull(mapper);
        this.changeDetector = checkNotNull(changeDetector);
    }

    @Nullable
    public final U getValue() {
        return value;
    }

    @Override
    public void logState(String when) {
        logger.debug("{}: {}={}", when, valueName, value);
    }

    @Override
    protected final boolean doWave() {
        @Nullable U newValue = mapper.apply(sourceNode);
        //noinspection unchecked
        if (changeDetector.isValueChanged((TN) this, value, newValue)) {
            logger.debug("{} {} -> {}", valueName, value, newValue);
            value = newValue;
            return true;
        }
        return false;
    }

    protected interface ChangeDetector<TN, U> {
        boolean isValueChanged(TN thisNode, U oldValue, U newValue);
    }
}
