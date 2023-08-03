package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

//TODO dynamic version - e.g. allow all types with a certain 'tag'
public final class VTemplateType extends VType {
    public final @Nullable Set<VType> bounds;

    public VTemplateType(@Nullable Set<VType> bounds) {
        this.bounds = bounds;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        VTemplateType that = (VTemplateType) o;
        return Objects.equals(this.bounds, that.bounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bounds);
    }
}
