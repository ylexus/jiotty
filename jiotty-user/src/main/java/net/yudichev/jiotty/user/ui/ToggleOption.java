package net.yudichev.jiotty.user.ui;

import net.yudichev.jiotty.common.async.TaskExecutor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ToggleOption extends BaseOption<Boolean> {

    private final String label;

    protected ToggleOption(TaskExecutor executor, String key, String label, boolean initiallySet) {
        super(executor, key, initiallySet);
        this.label = checkNotNull(label);
    }

    public final boolean isSet() {
        return getValue().orElse(Boolean.FALSE);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public final CompletableFuture<Void> onFormSubmit(Optional<String> value) {
        return setValue(value.isPresent());
    }

    @Override
    public OptionDtos.OptionDto toDto() {
        return new OptionDtos.Checkbox("checkbox", getKey(), label, tabName(), getFormOrder(), isSet());
    }
}
