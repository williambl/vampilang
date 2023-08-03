package com.williambl.vampilang.lang.type;

import java.util.HashMap;
import java.util.function.Predicate;

public sealed class SimpleVType implements VType permits TypedVType {
    private final Predicate<Object> predicate;

    SimpleVType(Predicate<Object> predicate) {
        this.predicate = predicate;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        return this;
    }

    @Override
    public boolean contains(VType other) {
        return other == this;
    }

    @Override
    public boolean accepts(Object value) {
        return this.predicate.test(value);
    }
}
