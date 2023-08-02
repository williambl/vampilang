package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public String toString(EvaluationContext ctx) {
        return super.toString(ctx) + (this.bounds == null ? "" : "["+this.bounds.stream().map(b -> b.toString(ctx)).sorted().collect(Collectors.joining("|"))+"]");
    }
}
