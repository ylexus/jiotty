package net.yudichev.jiotty.user.ui;

import net.yudichev.jiotty.common.async.TaskExecutor;
import net.yudichev.jiotty.common.lang.CompletableFutures;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TimeOption extends BaseOption<LocalTime> {
    private final String label;

    protected TimeOption(TaskExecutor executor, String key, String label, LocalTime defaultValue) {
        super(executor, key, defaultValue);
        this.label = checkNotNull(label);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public CompletableFuture<Void> onFormSubmit(Optional<String> value) {
        LocalTime localTime;
        try {
            localTime = value.map(LocalTime::parse).orElse(null);
        } catch (DateTimeParseException e) {
            return CompletableFutures.failure("Invalid time: '" + e.getParsedString() + "'");
        }
        return setValue(localTime);
    }

    @Override
    public OptionDtos.OptionDto toDto() {
        return new OptionDtos.Time("time", getKey(), label, tabName(), getFormOrder(), getValue().map(LocalTime::toString).orElse(null));
    }
}
