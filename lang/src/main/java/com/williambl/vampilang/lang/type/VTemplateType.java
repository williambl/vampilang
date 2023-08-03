package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

//TODO dynamic version - e.g. allow all types with a certain 'tag'
public final class VTemplateType implements VType {
    public final @Nullable Set<VType> bounds;

    VTemplateType(@Nullable Set<VType> bounds) {
        this.bounds = bounds;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        return new VTemplateType(this.bounds);
    }

    @Override
    public boolean contains(VType other) {
        return this.equals(other) || this.bounds == null || this.bounds.contains(other) || (other instanceof VTemplateType template && template.bounds != null && this.bounds.containsAll(template.bounds));
    }

    @Override
    public boolean accepts(Object value) {
        return this.bounds == null || this.bounds.stream().anyMatch(b -> b.accepts(value));
    }

    @Override
    public String toString(EvaluationContext ctx) {
        return VType.super.toString(ctx) + (this.bounds == null ? "" : "["+this.bounds.stream().map(b -> b.toString(ctx)).sorted().collect(Collectors.joining("|"))+"]");
    }
}
