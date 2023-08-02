package com.williambl.vampilang.lang.type;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class VTemplateType extends VType {
    private final @Nullable Set<VType> bounds;

    public VTemplateType(@Nullable Set<VType> bounds) {
        this.bounds = bounds;
    }

    @Override
    public VType uniquise() {
        return new VTemplateType(this.bounds);
    }

    @Override
    public boolean contains(VType other) {
        return this == other || this.bounds == null || this.bounds.contains(other) || (other instanceof VTemplateType template && template.bounds != null && this.bounds.containsAll(template.bounds));
    }
}
