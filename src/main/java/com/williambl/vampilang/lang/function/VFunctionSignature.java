package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.type.VType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public record VFunctionSignature(List<VType> inputTypes, VType outputType) {
    public VFunctionSignature uniquise() {
        var uniquisedTemplates = new HashMap<VType, VType>();
        for (var type : this.inputTypes) {
            uniquisedTemplates.computeIfAbsent(type, $ -> type.uniquise());
        }
        uniquisedTemplates.computeIfAbsent(this.outputType, $ -> this.outputType.uniquise());
        return new VFunctionSignature(this.inputTypes.stream().map(uniquisedTemplates::get).toList(), uniquisedTemplates.get(this.outputType));
    }

    public VFunctionSignature resolveTypes(List<VType> actualInputs) {
        var resolvedTemplates = new HashMap<VType, VType>();
        for (int i = 0; i < this.inputTypes.size(); i++) {
            var inputType = this.inputTypes.get(i);
            var actualInputType = actualInputs.get(i);
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
                this.inputTypes.stream().map(t -> resolvedTemplates.getOrDefault(t, t)).toList(),
                resolvedTemplates.getOrDefault(this.outputType, this.outputType)
        );
    }

    public String toString(EvaluationContext ctx) {
        var builder = new StringBuilder();
        for (int i = 0; i < this.inputTypes.size(); i++) {
            builder.append(this.inputTypes.get(i).toString(ctx));
            if (i+1 < this.inputTypes.size()) {
                builder.append(", ");
            }
        }
        builder.append(" -> ");
        builder.append(this.outputType.toString(ctx));
        return builder.toString();
    }
}
