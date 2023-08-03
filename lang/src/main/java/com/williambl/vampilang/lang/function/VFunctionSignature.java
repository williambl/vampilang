package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;

import java.util.*;
import java.util.stream.Collectors;

public record VFunctionSignature(Map<String, VType> inputTypes, VType outputType) {
    public VFunctionSignature uniquise() {
        var uniquisedTemplates = new HashMap<VType, VType>();
        for (var type : this.inputTypes.values()) {
            if (!uniquisedTemplates.containsKey(type)) {
                uniquisedTemplates.put(type, type.uniquise(uniquisedTemplates));
            }
        }
        uniquisedTemplates.computeIfAbsent(this.outputType, $ -> this.outputType.uniquise(uniquisedTemplates));
        return new VFunctionSignature(
                this.inputTypes.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, kv -> uniquisedTemplates.get(kv.getValue()))), uniquisedTemplates.get(this.outputType));
    }

    public VFunctionSignature resolveTypes(Map<String, VType> actualInputs) {
        var resolvedTemplates = new HashMap<VType, VType>();
        for (var key : this.inputTypes.keySet()) {
            var inputType = this.inputTypes.get(key);
            var actualInputType = actualInputs.get(key);
            if (!inputType.contains(actualInputType)) {
                throw new IllegalStateException("cannot reconcile %s and %s".formatted(inputType, actualInputs));
            }

            recursivelyResolveTypes(resolvedTemplates, inputType, actualInputType);
        }

        return new VFunctionSignature(
                this.inputTypes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> resolvedTemplates.getOrDefault(kv.getValue(), kv.getValue()))),
                resolvedTemplates.getOrDefault(this.outputType, this.outputType)
        );
    }

    private static void recursivelyResolveTypes(Map<VType, VType> resolvedTemplates, VType input, VType actual) {
        if (!actual.contains(input)) { // if actual input type is more specific
            resolvedTemplates.put(input, actual);
            if (input instanceof VParameterisedType paramed) {
                var actualParamed = (VParameterisedType) actual;
                if (paramed.parameters.size() != actualParamed.parameters.size()) {
                    throw new IllegalStateException("bug! there is no way %s can contain %s when they have different param types.".formatted(input, actual));
                }

                for (int i = 0; i < paramed.parameters.size(); i++) {
                    var inputParam = paramed.parameters.get(i);
                    var actualParam = actualParamed.parameters.get(i);
                    recursivelyResolveTypes(resolvedTemplates, inputParam, actualParam);
                }
            }
        }
    }

    public String toString(TypeNamer ctx) {
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
