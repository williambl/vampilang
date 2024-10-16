package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.VEnvironment;

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
    public boolean contains(VType other, VEnvironment env) {
        return other == this;
    }

    @Override
    public boolean accepts(Object value, VEnvironment env) {
        return this.predicate.test(value);
    }
}
