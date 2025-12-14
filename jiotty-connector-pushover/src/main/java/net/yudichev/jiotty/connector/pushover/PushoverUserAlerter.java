package net.yudichev.jiotty.connector.pushover;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;
import net.pushover.client.MessagePriority;
import net.pushover.client.PushoverException;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
import net.pushover.client.Status;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MINUTES;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;

final class PushoverUserAlerter extends BaseLifecycleComponent implements UserAlerter {
    private static final Logger logger = LoggerFactory.getLogger(PushoverUserAlerter.class);
    private final ExecutorFactory executorFactory;
    private final String apiToken;

    private PushoverRestClient pushoverClient;
    private CloseableHttpClient httpClient;
    private SchedulingExecutor executor;

    @Inject
    public PushoverUserAlerter(ExecutorFactory executorFactory, @ApiToken String apiToken) {
        this.executorFactory = checkNotNull(executorFactory);
        this.apiToken = checkNotNull(apiToken);
    }

    @Override
    public void sendAlert(User user, MessagePriority priority, String text) {
        whenStartedAndNotLifecycling(() -> executor.execute(() -> {
            try {
                logger.debug("Sending '{}' with priority {} to {}", text, priority, user);
                PushoverMessage.Builder builder = PushoverMessage.builderWithApiToken(apiToken)
                                                                 .setUserId(user.token())
                                                                 .setSound("updown")
                                                                 .setPriority(priority);
                if (priority == MessagePriority.EMERGENCY) {
                    builder.setRetry(30)
                           .setExpire((int) MINUTES.toSeconds(5));
                }
                Status status = pushoverClient.pushMessage(builder.setMessage(text).build());
                logger.info("Alert '{}' sent to {}, result: {}", text, user, status);
            } catch (PushoverException e) {
                logger.error("Failed sending alert to {}", user, e);
            }
        }));
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("PushoverUserAlerter");
        httpClient = HttpClients.createDefault();
        pushoverClient = new PushoverRestClient();
        pushoverClient.setHttpClient(httpClient);
    }

    @Override
    protected void doStop() {
        closeSafelyIfNotNull(logger, httpClient, executor);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ApiToken {
    }
}
