package net.jiotty.common.lang;

import org.immutables.value.Value;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target({PACKAGE, TYPE})
@Retention(CLASS) // Make it class retention for incremental compilation
@Value.Style(
        get = {"is*", "get*"}, // Detect 'get' and 'is' prefixes in accessor methods
        init = "set*", // Builder initialization methods will have 'set' prefix
        typeAbstract = {"Base*"}, // 'Abstract' prefix will be detected and trimmed
        typeImmutable = "*", // No prefix or suffix for generated immutable type
        visibility = Value.Style.ImplementationVisibility.PUBLIC)
public @interface PublicImmutablesStyle {
}
