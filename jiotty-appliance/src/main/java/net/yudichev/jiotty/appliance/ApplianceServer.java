package net.yudichev.jiotty.appliance;

import com.google.common.collect.Maps;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import net.yudichev.jiotty.common.rest.RestServer;
import net.yudichev.jiotty.common.rest.RestServers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class ApplianceServer extends BaseLifecycleComponent {
    private static final Logger logger = LoggerFactory.getLogger(ApplianceServer.class);

    private final RestServer restServer;
    private final String applianceId;
    private final Appliance appliance;

    @Inject
    ApplianceServer(@Dependency RestServer restServer,
                    @ApplianceId String applianceId,
                    @Dependency Appliance appliance) {
        this.restServer = checkNotNull(restServer);
        this.applianceId = checkNotNull(applianceId);
        this.appliance = checkNotNull(appliance);
    }

    @Override
    public String name() {
        return String.format("Server for %s @ %s", appliance.name(), System.identityHashCode(this));
    }

    @Override
    public void doStart() {
        appliance.getAllSupportedCommandMetadata().forEach(commandMeta -> {
            String url = "/appliance/" + applianceId + "/" + commandMeta.commandName().toLowerCase();
            logger.info("Registering {}", url);
            restServer.post(url,
                            (request, response) -> {
                                CompletableFuture<?> result;
                                try {
                                    var paramValues = Maps.<String, CommandParamType, Object>transformEntries(
                                            commandMeta.parameterTypes(),
                                            (name, paramType) -> paramType.decode(request.queryParamsSafe(name)));
                                    Command<?> command = commandMeta.createCommand(paramValues);
                                    logger.info("{} executing {}", applianceId, command);
                                    result = appliance.execute(command);
                                    result.whenComplete((r, throwable) -> logger.info("{} executed {}, result: {}", applianceId, command, r, throwable));
                                } catch (RuntimeException e) {
                                    result = CompletableFutures.failure(e);
                                }
                                return RestServers.withErrorsHandledJson(url, response, result);
                            });
        });
    }

    @Retention(RUNTIME)
    @Target({FIELD, PARAMETER, METHOD})
    @BindingAnnotation
    @interface ApplianceId {
    }

    @Retention(RUNTIME)
    @Target({FIELD, PARAMETER, METHOD})
    @BindingAnnotation
    @interface Dependency {
    }
}
