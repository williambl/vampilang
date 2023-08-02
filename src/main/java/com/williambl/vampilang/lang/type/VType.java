package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;

public sealed class VType permits VTemplateType {
    public final boolean isTemplate() {
        return this instanceof VTemplateType;
    }

    public VType uniquise() {
        return this;
    }

    public boolean contains(VType other) {
        return other == this;
    }

    public String toString(EvaluationContext ctx) {
        return ctx.name(this);
    }
}
