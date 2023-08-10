package com.williambl.vampilang.lang.function;

import com.mojang.serialization.DataResult;
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

    public DataResult<VFunctionSignature> resolveTypes(Map<String, VType> actualInputs) {
        var resolvedTemplates = new HashMap<VType, Set<VType>>();
        for (var key : this.inputTypes.keySet()) {
            var inputType = this.inputTypes.get(key);
            var actualInputType = actualInputs.get(key);
            if (!inputType.contains(actualInputType)) {
                return DataResult.error(() -> "cannot reconcile %s and %s".formatted(inputType, actualInputs));
            }

            recursivelyResolveTypes(resolvedTemplates, inputType, actualInputType);
        }

        var errors = new ArrayList<String>();
        var reducedResolvedTemplates = resolvedTemplates.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> {
            var types = kv.getValue();
            VType mostGeneral = null;
            for (var type : types) {
                if (mostGeneral == null) {
                    mostGeneral = type;
                    continue;
                }

                if (mostGeneral.contains(type)) {
                    continue;
                }

                if (type.contains(mostGeneral)) {
                    mostGeneral = type;
                    continue;
                }

                errors.add("cannot reconcile %s and %s".formatted(type, mostGeneral));
                mostGeneral = null;
            }

            if (mostGeneral == null) {
                errors.add("no resolution found for %s".formatted(kv.getKey()));
                mostGeneral = VType.create(); // dummy
            }

            return mostGeneral;
        }));

        if (!errors.isEmpty()) {
            return DataResult.error(() -> String.join("\n and ", errors));
        }

        return DataResult.success(new VFunctionSignature(
                this.inputTypes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> recursivelyRetrieveResolvedType(reducedResolvedTemplates, kv.getValue()))),
                recursivelyRetrieveResolvedType(reducedResolvedTemplates, this.outputType)
        ));
    }

    private static VType recursivelyRetrieveResolvedType(Map<VType, VType> resolvedTemplates, VType input) {
        if (resolvedTemplates.containsKey(input)) {
            return resolvedTemplates.get(input);
        }

        if (input instanceof VParameterisedType paramed) {
            var moreSpecificParams = paramed.parameters.stream().map(p -> recursivelyRetrieveResolvedType(resolvedTemplates, p)).toList();
            return paramed.with(moreSpecificParams);
        }

        return input;
    }

    private static void recursivelyResolveTypes(Map<VType, Set<VType>> resolvedTemplates, VType input, VType actual) {
        if (!actual.contains(input)) { // if actual input type is more specific
            resolvedTemplates.compute(input, (i, s) -> {
                var res = s == null ? new HashSet<VType>() : s;
                res.add(actual);
                return res;
            });
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
