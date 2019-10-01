package net.jiotty.common.inject;

import net.jiotty.common.lang.TypedBuilder;

public interface HasWithAnnotation {
    TypedBuilder<?> withAnnotation(SpecifiedAnnotation specifiedAnnotation);
}
