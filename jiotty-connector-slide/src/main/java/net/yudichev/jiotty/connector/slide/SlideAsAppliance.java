package net.yudichev.jiotty.connector.slide;

import com.google.common.collect.ImmutableSet;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.appliance.Command;
import net.yudichev.jiotty.appliance.PowerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.connector.slide.Bindings.SlideId;

final class SlideAsAppliance implements Appliance {
    private static final Logger logger = LoggerFactory.getLogger(SlideAsAppliance.class);

    private final SlideService slideService;
    private final long slideId;
    private final String name;

    @Inject
    SlideAsAppliance(@Dependency SlideService slideService,
                     @SlideId long slideId,
                     @Name String name) {
        this.slideService = checkNotNull(slideService);
        this.slideId = slideId;
        this.name = checkNotNull(name);
    }

    @Override
    public CompletableFuture<?> execute(Command command) {
        return command.acceptOrFail((PowerCommand.Visitor<CompletableFuture<?>>) powerCommand ->
                powerCommand == PowerCommand.ON ? slideService.closeSlide(slideId) : slideService.openSlide(slideId))
                .thenRun(() -> logger.info("Slide {}: executed {}", name, command));
    }

    @Override
    public Set<? extends Command> getAllSupportedCommands() {
        return ImmutableSet.copyOf(PowerCommand.values());
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Name {
    }
}
