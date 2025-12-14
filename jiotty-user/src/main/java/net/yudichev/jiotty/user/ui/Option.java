package net.yudichev.jiotty.user.ui;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.lang.Closeable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Option<T> {
    TypeToken<T> getValueType();

    String getKey();

    default String getLabel() {
        return getKey();
    }

    default int getFormOrder() {
        return 50;
    }

    String tabName();

    OptionDtos.OptionDto toDto();

    Optional<T> getValue();

    default T requireValue() {
        return getValue().orElseThrow(() -> new IllegalStateException(getLabel() + " is required"));
    }

    Closeable addChangeListener(Consumer<Option<T>> listener);

    CompletableFuture<Void> setValue(T value);

    CompletableFuture<Void> onFormSubmit(Optional<String> value);

    void applyDefault();

    void setValueSync(T value);
}
