package net.yudichev.jiotty.common.lang;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ResultOrFailure<T> {
    private static final Object NO_VALUE = new Object();
    private final Either<T, String> eitherResultOrFailure;

    private ResultOrFailure(Either<T, String> eitherResultOrFailure) {
        this.eitherResultOrFailure = checkNotNull(eitherResultOrFailure);
    }

    public static <T> ResultOrFailure<T> success(T value) {
        return new ResultOrFailure<>(Either.left(value));
    }

    public static ResultOrFailure<Object> success() {
        return new ResultOrFailure<>(Either.left(NO_VALUE));
    }

    public static <T> ResultOrFailure<T> failure(String error) {
        return new ResultOrFailure<>(Either.right(error));
    }

    public void accept(Consumer<? super T> resultConsumer,
                       Consumer<? super String> errorConsumer) {
        eitherResultOrFailure.accept(resultConsumer, errorConsumer);
    }

    public <U> U map(Function<? super T, ? extends U> resultMapper,
                     Function<? super String, ? extends U> errorMapper) {
        return eitherResultOrFailure.map(resultMapper, errorMapper);
    }

    public Optional<T> toSuccess() {
        return eitherResultOrFailure.getLeft();
    }

    public Optional<String> toFailure() {
        return eitherResultOrFailure.getRight();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ResultOrFailure<?> other = (ResultOrFailure<?>) obj;
        return eitherResultOrFailure.equals(other.eitherResultOrFailure);
    }

    @Override
    public int hashCode() {
        return eitherResultOrFailure.hashCode();
    }
}
