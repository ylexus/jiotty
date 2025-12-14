package net.yudichev.jiotty.user.ui;

import com.google.common.collect.ImmutableList;
import net.yudichev.jiotty.common.async.TaskExecutor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class SelectOption extends BaseOption<String> {

    private final String label;
    private final List<String> options;

    protected SelectOption(TaskExecutor executor, String key, String label, Iterable<String> options, String defaultOption) {
        super(executor, key, defaultOption);
        this.label = checkNotNull(label);
        this.options = ImmutableList.copyOf(options);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public final CompletableFuture<Void> onFormSubmit(Optional<String> value) {
        return setValue(value.orElse(null));
    }

    @Override
    public OptionDtos.OptionDto toDto() {
        return new OptionDtos.Select("select", getKey(), label, tabName(), getFormOrder(), options, getValue().orElse(null));
    }
}
