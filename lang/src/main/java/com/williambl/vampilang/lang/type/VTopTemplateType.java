package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.VEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VTopTemplateType extends VDynamicTemplateType {
    VTopTemplateType() {
        super($ -> true);
    }

    @Override
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        return new VTopTemplateType();
    }

    @Override
    public Stream<VType> bounds(VEnvironment env) {
        return env.allTypes().values().stream();
    }

    @Override
    public String toString(TypeNamer ctx) {
        return ctx.name(this) + "[all]";
    }
}
