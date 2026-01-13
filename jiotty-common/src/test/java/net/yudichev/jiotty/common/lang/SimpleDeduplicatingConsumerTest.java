package net.yudichev.jiotty.common.lang;

import java.util.function.Consumer;

final class SimpleDeduplicatingConsumerTest extends DeduplicatingConsumerTest {
    @Override
    protected Consumer<String> createInstance(EqualityComparator<String> equalityComparator, Consumer<String> delegate) {
        return new DeduplicatingConsumer<>(equalityComparator, delegate);
    }
}
