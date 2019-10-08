package net.yudichev.jiotty.common.lang;

import java.util.Optional;
import java.util.function.Consumer;

public final class Optionals {
    private Optionals() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // as designed
    public static <T> OrElse ifPresent(Optional<? extends T> optional,
                                       Consumer<? super T> presentValueConsumer) {
        if (optional.isPresent()) {
            presentValueConsumer.accept(optional.get());
            return action -> {};
        } else {
            return Runnable::run;
        }
    }

    public interface OrElse {
        void orElse(Runnable action);
    }
}
