package net.yudichev.jiotty.common.inject;

import net.yudichev.jiotty.common.lang.TypedBuilder;

public interface HasWithAnnotation {
    TypedBuilder<?> withAnnotation(SpecifiedAnnotation specifiedAnnotation);
}
