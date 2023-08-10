package com.williambl.vampilang.lang.type;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.VValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public sealed interface VType permits SimpleVType, VParameterisedType, VTemplateType {
    VType uniquise(HashMap<VType, VType> uniquisedTemplates);

    boolean contains(VType other);

    boolean accepts(Object value);

    default String toString(TypeNamer ctx) {
        return ctx.name(this);
    }

    static <T> TypedVType<T> create(TypeToken<T> typeToken) {
        return new TypedVType<>(typeToken);
    }

    static <T> ConstructableVType<T> create(TypeToken<T> typeToken, Map<String, VType> propertyTypes, Function<Map<String, VValue>, T> constructor) {
        return new ConstructableVType<>(typeToken, constructor, Map.copyOf(propertyTypes));
    }

    static SimpleVType create() {
        return new SimpleVType($ -> true);
    }

    static SimpleVType create(Predicate<Object> predicate) {
        return new SimpleVType(predicate);
    }

    static VTemplateType createTemplate() {
        return new VTemplateType(null);
    }

    static VTemplateType createTemplate(VType... bounds) {
        return new VTemplateType(Set.of(bounds));
    }

    static VParameterisedType createParameterised(VType bare, VType... typeParams) {
        return new VParameterisedType(bare, List.of(typeParams), (t, o) -> bare.accepts(o));
    }

    static LambdaVType createLambda(VType bare, VType resultType, EvaluationContext.Spec spec) {
        return new LambdaVType(bare, resultType, spec);
    }
}
