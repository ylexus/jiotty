package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.CompletableFutures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static net.yudichev.jiotty.common.testutil.AssertionArgumentMatcher.assertArg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncOperationRetryImplTest {
    @Mock
    private Supplier<CompletableFuture<String>> action;
    private AsyncOperationRetryImpl retry;
    @Mock
    private AsyncOperationFailureHandler backoffHandler;
    @Mock
    private BiConsumer<Long, Runnable> backoffEventConsumer;

    @BeforeEach
    void setUp() {
        retry = new AsyncOperationRetryImpl(backoffHandler);
    }

    @Test
    void actionCompletesFirstTime() {
        when(action.get()).thenReturn(completedFuture("Potter"));

        var resultFuture = retry.withBackOffAndRetry("operationName", action, backoffEventConsumer);
        assertThat(resultFuture.getNow(null), is("Potter"));
        verify(backoffEventConsumer, never()).accept(any(), any());
        verify(backoffHandler, never()).handle(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"result", "null"})
    void actionCompletesFromThirdAttempt(String result) throws Exception {
        if ("null".equals(result)) {
            result = null;
        }
        var invocationFuture1 = new CompletableFuture<String>();
        var invocationFuture2 = CompletableFutures.<String>failure("error 2");
        var invocationFuture3 = completedFuture(result);
        when(action.get()).thenReturn(invocationFuture1, invocationFuture2, invocationFuture3);

        // attempt 1
        resetMocks();
        var resultFuture = retry.withBackOffAndRetry("operationName", action, backoffEventConsumer);
        assertThat(resultFuture.isDone(), is(false));
        verify(backoffEventConsumer, never()).accept(any(), any());
        verify(backoffHandler, never()).handle(any(), any());

        var retryActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        invocationFuture1.completeExceptionally(new RuntimeException("error 1"));
        verify(backoffHandler).handle(eq("operationName"), assertArg(e -> assertThat(e.getMessage(), containsString("error 1"))));
        verify(backoffEventConsumer).accept(eq(100L), retryActionCaptor.capture());

        // attempt 2
        resetMocks();
        retryActionCaptor.getValue().run();
        verify(backoffHandler).handle(eq("operationName"), assertArg(e -> assertThat(e.getMessage(), containsString("error 2"))));
        retryActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(backoffEventConsumer).accept(eq(100L), retryActionCaptor.capture());
        assertThat(resultFuture.isDone(), is(false));

        // attempt 3 = success
        resetMocks();
        retryActionCaptor.getValue().run();
        verify(backoffHandler).reset();
        verifyNoMoreInteractions(backoffEventConsumer);

        assertThat(resultFuture.isDone(), is(true));
        assertThat(resultFuture.get(), is(result));
    }

    void resetMocks() {
        reset(backoffEventConsumer, backoffHandler);
        lenient().when(backoffHandler.handle(eq("operationName"), any())).thenReturn(Optional.of(100L));
    }
}