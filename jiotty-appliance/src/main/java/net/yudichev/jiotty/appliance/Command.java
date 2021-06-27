package net.yudichev.jiotty.appliance;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OverloadedVarargsMethod") // no confusion possible here
public interface Command<T extends Command<T>> {
    <U> Optional<U> accept(Visitor<U> visitor);

    default <U> U acceptOrFail(Visitor<U> visitor) {
        return accept(visitor).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Command %s is not supported", this)));
    }

    @SuppressWarnings("unchecked")
    default <U> Optional<U> accept(Visitor<U>... visitors) {
        return Stream.of(visitors)
                .map(this::accept)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    default <U> U acceptOrFail(Visitor<U>... visitors) {
        return accept(visitors).orElseThrow(() -> new IllegalArgumentException(String.format(
                "Command %s is not supported", this)));
    }

    interface Visitor<T> {
    }
}
