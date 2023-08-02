package com.williambl;

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
}
