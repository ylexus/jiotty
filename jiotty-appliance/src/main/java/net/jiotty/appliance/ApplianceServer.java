package net.jiotty.appliance;

import com.google.inject.BindingAnnotation;
import net.jiotty.common.inject.BaseLifecycleComponent;
import net.jiotty.common.rest.RestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.jiotty.common.rest.RestServers.withErrorsHandledJson;

final class ApplianceServer extends BaseLifecycleComponent {
    private static final Logger logger = LoggerFactory.getLogger(ApplianceServer.class);

    private final RestServer restServer;
    private final String applianceId;
    private final Appliance appliance;

    @Inject
    ApplianceServer(RestServer restServer,
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
        appliance.getAllSupportedCommands().forEach(command -> {
            String url = "/appliance/" + applianceId + "/" + command.name().toLowerCase();
            logger.info("Registering {}", url);
            restServer.post(url,
                    (request, response) -> {
                        logger.info("{} executing {}", applianceId, command);
                        Object responseData = withErrorsHandledJson(url, response, appliance.execute(command));
                        logger.info("{} is {}", applianceId, command);
                        return responseData;
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
