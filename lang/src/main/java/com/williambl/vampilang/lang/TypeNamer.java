package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.type.VType;

import java.util.HashMap;
import java.util.Map;

public class TypeNamer {
    private final Map<VType, String> typeNames = new HashMap<>();
    private int anonymousTypeNumberCount = 0;

    public TypeNamer() {
    }

    public TypeNamer(TypeNamer copyOf) {
        this();
        this.typeNames.putAll(copyOf.typeNames);
        this.anonymousTypeNumberCount = copyOf.anonymousTypeNumberCount;
    }

    public TypeNamer addName(VType type, String name) {
        this.typeNames.put(type, name);
        return this;
    }

    public String name(VType type) {
        return this.typeNames.computeIfAbsent(type, $ -> "type"+ ++this.anonymousTypeNumberCount);
    }
}
