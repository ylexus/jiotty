package net.yudichev.jiotty.common.graph.server;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import jakarta.annotation.Nullable;
import net.yudichev.jiotty.common.lang.Closeable;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNullElse;

public abstract class BaseDeviceCommandRequestNode<R> extends BaseServerNode implements DeviceCommandRequestNode<R> {
    private final Duration retryDelay;
    private final boolean panicOnFailure;
    private final int maxRetriesBeforePanic;
    private @Nullable DeviceRequest<R> request;
    private @Nullable Closeable pendingRequestRetrySchedule;
    private boolean retryDue;
    private int retryCount;
    @Nullable
    private String lastFailure;

    protected BaseDeviceCommandRequestNode(GraphRunner runner, String name, Duration retryDelay, int maxRetriesBeforePanic, boolean panicOnFailure) {
        super(runner, name);
        this.retryDelay = checkNotNull(retryDelay);
        this.panicOnFailure = panicOnFailure;
        checkArgument(maxRetriesBeforePanic >= 0);
        this.maxRetriesBeforePanic = maxRetriesBeforePanic;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean doWave() {
        if (request == null) {
            resetRetryState();
            return false;
        }

        if (shouldForgetRequest()) {
            resetRetryState();
            request = null;
            return true;
        }

        if (request.sent()) {
            if (deviceStateIndicatesRequestSuccessful(request.payload())) {
                logger.info("Successfully executed: {}", request);
                request = null;
                resetRetryState();
                return true;
            } else {
                if (retryDue) {
                    retryDue = false;
                    if (++retryCount > maxRetriesBeforePanic) {
                        onCommandFailedFatally(lastFailure);
                        var failure = requireNonNullElse(lastFailure,
                                                         "Retry count exceeded: device state did not confirm that the command was successful");
                        request = request.asFailed(failure);
                        if (panicOnFailure) {
                            runner.panic(request + " retried " + (retryCount - 1) + " times, last failure: " + failure);
                        }
                    } else if (deviceStateValidForRequestToBeSent()) {
                        logger.info("Retry {}/{} of {}", retryCount, maxRetriesBeforePanic, request);
                        doExecuteRequest(retryCount);
                        return true;
                    } else {
                        logger.debug("Awaiting valid device state before retrying the request");
                        return false;
                    }
                    return true;
                } else {
                    logger.info("Awaiting the effect of {}", request);
                    return false;
                }
            }
        } else if (deviceStateValidForRequestToBeSent()) {
            logger.info("Executing request {}", request);
            request = request.asSent();
            doExecuteRequest(retryCount);
            return true;
        } else {
            logger.debug("Awaiting valid device state for sending request");
            return false;
        }
    }

    @Override
    public void close() {
        Closeable.closeIfNotNull(pendingRequestRetrySchedule);
        super.close();
    }

    @Override
    public final void logState(String when) {
        if (logger.isDebugEnabled()) {
            var additionalStateKey = additionalLoggingStateKey();
            var additionalStateValue = additionalLoggingStateValue();
            logger.debug("{}: request={}, pendingRequestRetrySchedule={}, retryDue={}, retryCount={}, lastFailure={}{}{}", when, request,
                         pendingRequestRetrySchedule, retryDue, retryCount, lastFailure,
                         additionalStateKey == null ? "" : additionalStateKey,
                         additionalStateValue == null ? "" : additionalStateValue);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Override
    public final boolean requestPending() {
        return request != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Nullable
    @Override
    public final DeviceRequest<R> currentRequest() {
        return request;
    }

    /**
     * Called in-wave before anything else - return true to immediately forget the request and reset to an idle state
     */
    protected boolean shouldForgetRequest() {
        return false;
    }

    protected void onCommandFailedFatally(String lastFailure) {
    }

    protected @Nullable Object additionalLoggingStateKey() {
        return null;
    }

    protected @Nullable Object additionalLoggingStateValue() {
        return null;
    }

    protected final void createRequestIfNotAlreadyInProgress(Supplier<DeviceRequest<R>> requestFactory) {
        var newRequest = requestFactory.get();
        if (request == null) {
            request = newRequest;
            triggerInNewWave(name() + " processing new request");
        } else if (!Objects.equals(request.payload(), newRequest.payload())) {
            request = newRequest;
            resetRetryState();
            triggerInNewWave(name() + " processing replaced request");
        } else {
            logger.debug("Same request already in progress: {}", request);
        }
    }

    protected abstract boolean deviceStateValidForRequestToBeSent();

    protected abstract boolean deviceStateIndicatesRequestSuccessful(R payload);

    protected abstract void sendCommand(int retryNumber, R payload, Consumer<String> failureHandler);

    private void resetRetryState() {
        Closeable.closeIfNotNull(pendingRequestRetrySchedule);
        pendingRequestRetrySchedule = null;
        retryDue = false;
        retryCount = 0;
        lastFailure = null;
    }

    private void doExecuteRequest(int retryNumber) {
        assert request != null;
        DeviceRequest<R> requestSent = request;
        sendCommand(retryNumber, request.payload(), failure -> {
            if (request != null) {
                logger.info("{} failure, will be retried: {}", request, failure);
                lastFailure = failure;
            } else {
                logger.debug("Ignoring delayed failure - request {} already inactive: {}", requestSent, failure);
            }
        });
        assert pendingRequestRetrySchedule == null;
        pendingRequestRetrySchedule = runner.executor().schedule(retryDelay, () -> {
            pendingRequestRetrySchedule = null;
            retryDue = true;
            triggerMeAndParentsInNewWave("Retry of " + request);
        });
    }
}
