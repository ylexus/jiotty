package net.yudichev.jiotty.user.ui;

import jakarta.inject.Inject;
import net.yudichev.jiotty.common.varstore.VarStore;

import static com.google.common.base.Preconditions.checkNotNull;

final class OptionPersistenceImpl implements OptionPersistence {
    private static final String UI_OPTIONS_KEY_PREFIX = "UiOption";

    private final VarStore varStore;

    @Inject
    OptionPersistenceImpl(VarStore varStore) {
        this.varStore = checkNotNull(varStore);
    }

    @Override
    public void save(Option<?> option) {
        varStore.saveValue(UI_OPTIONS_KEY_PREFIX + '.' + option.getKey(), option.requireValue());
    }

    @Override
    public <T> void load(Option<T> option) {
        varStore.readValue(option.getValueType(), UI_OPTIONS_KEY_PREFIX + '.' + option.getKey())
                .ifPresentOrElse(option::setValueSync, option::applyDefault);
    }
}
