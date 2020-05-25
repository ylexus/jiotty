package net.yudichev.jiotty.common.lang;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
class CompletableFuturesTest {

    @Test
    void toFutureOfList() {
        CompletableFuture<String> future1 = new CompletableFuture<>();
        CompletableFuture<String> future2 = new CompletableFuture<>();

        CompletableFuture<List<String>> result = IntStream.rangeClosed(1, 2)
                .mapToObj(value -> value == 1 ? future1 : future2)
                .collect(CompletableFutures.toFutureOfList());

        assertThat(result.isDone(), is(false));

        future1.complete("result1");
        assertThat(result.isDone(), is(false));

        future2.complete("result2");
        assertThat(result.isDone(), is(true));
        assertThat(result.getNow(null), Matchers.contains("result1", "result2"));
    }

    @Test
    @MockitoSettings(strictness = LENIENT)
    void toFutureOfListChaining(@Mock Function<? super Integer, CompletableFuture<String>> operation) {
        CompletableFuture<String> future1 = new CompletableFuture<>();
        when(operation.apply(1)).thenReturn(future1);
        CompletableFuture<String> future2 = new CompletableFuture<>();
        when(operation.apply(2)).thenReturn(future2);

        CompletableFuture<List<String>> result = IntStream.rangeClosed(1, 2)
                .boxed()
                .collect(CompletableFutures.toFutureOfListChaining(operation));

        verify(operation).apply(1);
        verify(operation, never()).apply(2);
        assertThat(result.isDone(), is(false));

        future1.complete("result1");
        verify(operation).apply(2);
        assertThat(result.isDone(), is(false));

        future2.complete("result2");
        assertThat(result.isDone(), is(true));
        assertThat(result.getNow(null), Matchers.contains("result1", "result2"));
    }
}