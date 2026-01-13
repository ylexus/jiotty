package net.yudichev.jiotty.common.lang;

import java.util.function.Consumer;

import static net.yudichev.jiotty.common.lang.EqualityComparator.equality;

final class ConcurrentDeduplicatingConsumerTest extends DeduplicatingConsumerTest {
    @Override
    protected Consumer<String> createInstance(EqualityComparator<String> equalityComparator, Consumer<String> delegate) {
        return new ConcurrentDeduplicatingConsumer<>(equality(), delegate);
    }
}
