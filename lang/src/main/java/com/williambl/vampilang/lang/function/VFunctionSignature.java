package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.type.VType;

import java.util.*;
import java.util.stream.Collectors;

public record VFunctionSignature(Map<String, VType> inputTypes, VType outputType) {
    public VFunctionSignature uniquise() {
        var uniquisedTemplates = new HashMap<VType, VType>();
        for (var type : this.inputTypes.values()) {
            uniquisedTemplates.computeIfAbsent(type, $ -> type.uniquise());
        }
        uniquisedTemplates.computeIfAbsent(this.outputType, $ -> this.outputType.uniquise());
        return new VFunctionSignature(
                this.inputTypes.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, kv -> uniquisedTemplates.get(kv.getValue()))), uniquisedTemplates.get(this.outputType));
    }

    public VFunctionSignature resolveTypes(Map<String, VType> actualInputs) {
        var resolvedTemplates = new HashMap<VType, VType>();
        for (var key : this.inputTypes.keySet()) {
            var inputType = this.inputTypes.get(key);
            var actualInputType = actualInputs.get(key);
            if (inputType.isTemplate() && inputType.contains(actualInputType)) {
                var currentResolution = resolvedTemplates.get(inputType);
                if (currentResolution != null && !actualInputType.contains(currentResolution)) {
                    throw new IllegalStateException("cannot reconcile %s and %s".formatted(currentResolution, actualInputs));
                } else if (currentResolution == null || actualInputType.contains(currentResolution)) {
                    resolvedTemplates.put(inputType, actualInputType);
                }
            }
        }

        return new VFunctionSignature(
                this.inputTypes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> resolvedTemplates.getOrDefault(kv.getValue(), kv.getValue()))),
                resolvedTemplates.getOrDefault(this.outputType, this.outputType)
        );
    }

    public String toString(EvaluationContext ctx) {
        var builder = new StringBuilder();
        var entries = new ArrayList<>(this.inputTypes.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        for (var iter = entries.iterator(); iter.hasNext();) {
            var input = iter.next();
            builder.append(input.getKey());
            builder.append(" : ");
            builder.append(input.getValue().toString(ctx));
            if (iter.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(" -> ");
        builder.append(this.outputType.toString(ctx));
        return builder.toString();
    }
}
