package net.yudichev.jiotty.user.ui;

interface OptionPersistence {
    void save(Option<?> option);

    <T> void load(Option<T> option);
}
