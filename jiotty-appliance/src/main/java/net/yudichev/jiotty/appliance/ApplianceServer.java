package net.yudichev.jiotty.appliance;

import com.google.common.collect.Maps;
import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import net.yudichev.jiotty.common.rest.RestServer;
import net.yudichev.jiotty.common.rest.RestServers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;
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
                            context -> {
                                CompletableFuture<?> result;
                                try {
                                    var command = createCommand(commandMeta, context.req());
                                    logger.info("{} executing {}", applianceId, command);
                                    result = appliance.execute(command);
                                    result.whenComplete((r, throwable) -> logger.info("{} executed {}, result: {}", applianceId, command, r, throwable));
                                } catch (RuntimeException e) {
                                    result = CompletableFutures.failure(e);
                                }
                                context.result(RestServers.withErrorsHandledJson(url, context.res(), result));
                            });
        });
    }

    private static Command<?> createCommand(CommandMeta<?> commandMeta, HttpServletRequest request) {
        var paramValues = Maps.<String, CommandParamType, Object>transformEntries(
                commandMeta.parameterTypes(),
                (name, paramType) -> {
                    try {
                        var param = request.getParameter(name);
                        checkArgument(param != null, "Missing required parameter '%s'", name);
                        return paramType.decode(param);
                    } catch (RuntimeException e) {
                        throw new RuntimeException("Failed decoding parameter " + name, e);
                    }
                });
        return commandMeta.createCommand(paramValues);
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
