package com.williambl.vampilang.lang.type;

import com.williambl.vampilang.lang.EvaluationContext;

import java.util.HashMap;

//TODO put a predicate or something in here
//TODO specialised VTYpe which wraps a TypeToken for quick casting
public sealed class VType permits VParameterisedType, VTemplateType {
    public VType uniquise(HashMap<VType, VType> uniquisedTemplates) {
        return this;
    }

    public boolean contains(VType other) {
        return other == this;
    }

    public String toString(EvaluationContext ctx) {
        return ctx.name(this);
    }
}
