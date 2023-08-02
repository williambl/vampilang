package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.type.VType;

import java.util.HashMap;
import java.util.Map;

public class EvaluationContext {
    private final Map<VType, String> typeNames = new HashMap<>();
    private int anonymousTypeNumberCount = 0;

    public String name(VType type) {
        return this.typeNames.computeIfAbsent(type, $ -> "type"+ ++this.anonymousTypeNumberCount);
    }
}
