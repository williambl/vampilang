package com.williambl;

import java.util.List;
import java.util.Set;

public final class VTemplateType extends VType {
    private final Set<VType> bounds;

    public VTemplateType(Set<VType> bounds) {
        this.bounds = bounds;
    }

    @Override
    public VType uniquise() {
        return new VTemplateType(this.bounds);
    }

    @Override
    public boolean contains(VType other) {
        return this == other || this.bounds.contains(other) || other instanceof VTemplateType template && this.bounds.containsAll(template.bounds);
    }
}
