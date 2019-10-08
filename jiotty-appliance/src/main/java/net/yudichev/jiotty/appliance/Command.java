package net.yudichev.jiotty.appliance;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("OverloadedVarargsMethod") // no confusion possible here
public interface Command {
    <T> Optional<T> accept(Visitor<T> visitor);

    default String name() {
        return toString();
    }

    default <T> T acceptOrFail(Visitor<T> visitor) {
        return accept(visitor).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Command %s is not supported", this)));
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<T> accept(Visitor<T>... visitors) {
        return Stream.of(visitors)
                .map(this::accept)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    default <T> T acceptOrFail(Visitor<T>... visitors) {
        return accept(visitors).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Command %s is not supported", this)));
    }

    default <T> T map(Function<? super Command, T> mapper) {
        return mapper.apply(this);
    }

    interface Visitor<T> {
    }
}
