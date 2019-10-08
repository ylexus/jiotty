package net.jiotty.common.lang.throttling;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.jiotty.common.inject.*;
import net.jiotty.common.lang.TypedBuilder;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;

public final class ThresholdThrottlingConsumerModule<T> extends BaseLifecycleComponentModule implements ExposedKeyModule<ThresholdThrottlingConsumerFactory<T>> {
    private final Key<ThresholdThrottlingConsumerFactory<T>> exposedKey;
    private final TypeToken<T> valueType;

    private ThresholdThrottlingConsumerModule(TypeToken<T> valueType, SpecifiedAnnotation specifiedAnnotation) {
        this.valueType = checkNotNull(valueType);
        exposedKey = specifiedAnnotation.specify(asReifiedTypeLiteral(new TypeToken<ThresholdThrottlingConsumerFactory<T>>() {}));
    }

    @Override
    public Key<ThresholdThrottlingConsumerFactory<T>> getExposedKey() {
        return exposedKey;
    }

    public static ValueChoiceBuilder builder() {
        return new ValueChoiceBuilder();
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(asReifiedTypeLiteral(new TypeToken<Consumer<T>>() {}), asReifiedTypeLiteral(new TypeToken<ThresholdThrottlingConsumer<T>>() {}))
                .build(exposedKey));

        expose(exposedKey);
    }

    private <U> TypeLiteral<U> asReifiedTypeLiteral(TypeToken<U> typeToken) {
        return TypeLiterals.asTypeLiteral(typeToken.where(new TypeParameter<T>() {}, valueType));
    }

    public static final class ValueChoiceBuilder {
        public <T> Builder<T> setValueType(Class<T> valueType) {
            return setValueType(TypeToken.of(valueType));
        }

        @SuppressWarnings("MethodMayBeStatic")
        private <T> Builder<T> setValueType(TypeToken<T> valueType) {
            return new Builder<>(valueType);
        }
    }

    public static final class Builder<T> implements TypedBuilder<ExposedKeyModule<ThresholdThrottlingConsumerFactory<T>>>, HasWithAnnotation {
        private final TypeToken<T> valueType;
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();

        private Builder(TypeToken<T> valueType) {
            this.valueType = checkNotNull(valueType);
        }

        @Override
        public Builder<T> withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<ThresholdThrottlingConsumerFactory<T>> build() {
            return new ThresholdThrottlingConsumerModule<>(valueType, specifiedAnnotation);
        }
    }

}
