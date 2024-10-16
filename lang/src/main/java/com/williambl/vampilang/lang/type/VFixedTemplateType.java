package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.VEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VFixedTemplateType extends VDynamicTemplateType {
    public final @NotNull Set<VType> bounds;

    VFixedTemplateType(@NotNull @Unmodifiable Set<VType> bounds) {
        super(bounds::contains);
        this.bounds = bounds;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        return new VFixedTemplateType(this.bounds);
    }

    @Override
    public Stream<VType> bounds(VEnvironment env) {
        return this.bounds.stream();
    }

    @Override
    public String toString(TypeNamer ctx) {
        return ctx.name(this) + "[" + this.bounds.stream().map(b -> b.toString(ctx)).sorted().collect(Collectors.joining("|")) + "]";
    }
}
