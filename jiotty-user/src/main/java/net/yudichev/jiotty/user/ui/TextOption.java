package net.yudichev.jiotty.user.ui;

import net.yudichev.jiotty.common.async.TaskExecutor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TextOption extends BaseOption<String> {
    private final String label;

    protected TextOption(TaskExecutor executor, String key, String label, String defaultValue) {
        super(executor, key, defaultValue);
        this.label = checkNotNull(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public CompletableFuture<Void> onFormSubmit(Optional<String> value) {
        return setValue(value.orElse(null));
    }

    @Override
    public OptionDtos.OptionDto toDto() {
        return new OptionDtos.Text("text", getKey(), label, tabName(), getFormOrder(), getValue().orElse(null));
    }
}
