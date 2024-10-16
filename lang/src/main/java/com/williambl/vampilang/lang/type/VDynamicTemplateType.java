package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.VEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed class VDynamicTemplateType implements VTemplateType permits VFixedTemplateType {
    public final @NotNull Predicate<VType> predicate;

    VDynamicTemplateType(@NotNull Predicate<VType> predicate) {
        this.predicate = predicate;
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        return new VDynamicTemplateType(this.predicate);
    }

    @Override
    public boolean contains(VType other, VEnvironment env) {
        return this.equals(other)
                || this.predicate.test(other)
                || other instanceof VTemplateType template && template.bounds(env).allMatch(this.predicate);
    }

    @Override
    public boolean accepts(Object value, VEnvironment env) {
        return this.bounds(env).anyMatch(b -> b.accepts(value, env));
    }

    @Override
    public String toString(TypeNamer ctx) {
        return ctx.name(this) + ("[dynamic]");
    }

    @Override
    public Stream<VType> bounds(VEnvironment env) {
        return env.allTypes().values().stream().filter(this.predicate);
    }
}
