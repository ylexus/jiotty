package net.yudichev.jiotty.connector.google.assistant;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.assistant.embedded.v1alpha2.*;
import com.google.auth.Credentials;
import com.google.inject.BindingAnnotation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.auth.MoreCallCredentials;
import io.grpc.stub.StreamObserver;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

final class GoogleAssistantClientImpl extends BaseLifecycleComponent implements GoogleAssistantClient {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAssistantClientImpl.class);

    private static final String SCOPE_ASSISTANT = "https://www.googleapis.com/auth/assistant-sdk-prototype";
    private static final String HOSTNAME = "embeddedassistant.googleapis.com";
    private final ResolvedGoogleApiAuthSettings settings;
    private final AudioOutConfig audioOutConfig;
    private final DialogStateIn dialogStateIn;
    private final DeviceConfig deviceConfig;

    private EmbeddedAssistantGrpc.EmbeddedAssistantStub stub;
    private ManagedChannel channel;

    @Inject
    GoogleAssistantClientImpl(@Settings ResolvedGoogleApiAuthSettings settings,
                              @Dependency AudioOutConfig audioOutConfig,
                              @Dependency DialogStateIn dialogStateIn,
                              @Dependency DeviceConfig deviceConfig) {
        this.settings = checkNotNull(settings);
        this.audioOutConfig = checkNotNull(audioOutConfig);
        this.dialogStateIn = checkNotNull(dialogStateIn);
        this.deviceConfig = checkNotNull(deviceConfig);
    }

    @Override
    public CompletableFuture<byte[]> assist(String phrase) {
        return whenStartedAndNotLifecycling(() -> {
            CompletableFuture<byte[]> result = new CompletableFuture<>();
            @SuppressWarnings("resource") ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamObserver<AssistRequest> requestStreamObserver = stub.assist(new StreamObserver<AssistResponse>() {
                @Override
                public void onNext(AssistResponse v) {
                    logger.debug("assist response onNext: {}", v);
                    if (v.hasAudioOut()) {
                        asUnchecked(() -> v.getAudioOut().writeTo(outputStream));
                    }
                    result.complete(null);
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.debug("assist response onError", throwable);
                    result.completeExceptionally(throwable);
                }

                @Override
                public void onCompleted() {
                    logger.debug("assist response onCompleted");
                    result.complete(outputStream.toByteArray());
                }
            });

            AssistRequest assistRequest = AssistRequest.newBuilder()
                    .setConfig(AssistConfig.newBuilder()
                            .setTextQuery(phrase)
                            .setAudioOutConfig(audioOutConfig)
                            .setDialogStateIn(dialogStateIn)
                            .setDeviceConfig(deviceConfig)
                            .build())
                    .build();

            logger.debug("Issuing assist request: {}", assistRequest);
            requestStreamObserver.onNext(assistRequest);

            return result;
        });
    }

    @Override
    protected void doStart() {
        Credentials credentials = GoogleAuthorization.builder()
                .setHttpTransport(getAsUnchecked(GoogleNetHttpTransport::newTrustedTransport))
                .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                .setApiName("assistant")
                .setCredentialsUrl(settings.credentialsUrl())
                .addRequiredScope(SCOPE_ASSISTANT)
                .withBrowser(settings.authorizationBrowser())
                .build()
                .getCredentials();

        channel = ManagedChannelBuilder.forTarget(HOSTNAME).build();
        stub = EmbeddedAssistantGrpc.newStub(channel)
                .withCallCredentials(MoreCallCredentials.from(credentials));
    }

    @Override
    protected void doStop() {
        if (channel != null) {
            boolean success = getAsUnchecked(() -> channel.shutdown().awaitTermination(5, TimeUnit.SECONDS));
            if (!success) {
                logger.warn("Were not able to shut down Assistant channel in 5 seconds");
            }
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
