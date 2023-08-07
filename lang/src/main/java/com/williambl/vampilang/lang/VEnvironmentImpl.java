package com.williambl.vampilang.lang;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.williambl.vampilang.codec.FunctionApplicationCodec;
import com.williambl.vampilang.codec.VExpressionCodec;
import com.williambl.vampilang.codec.ValueCodec;
import com.williambl.vampilang.codec.VariableRefCodec;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.SimpleVType;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VEnvironmentImpl implements VEnvironment {
    private final Map<String, VType> types = new HashMap<>();
    private final Map<VType, Codec<?>> codecs = new HashMap<>();
    private final Map<VType, Function<VParameterisedType, Codec<?>>> parameterisedTypeCodecs = new HashMap<>();
    private final Map<String, VFunctionDefinition> functions = new HashMap<>();
    private final Map<TypeAndSpecCacheKey, Codec<VExpression>> cachedVExpressionCodecs = new HashMap<>();

    @Override
    public Codec<?> rawCodecForType(VType type) {
        var res = this.codecs.get(type);
        if (res == null && type instanceof VParameterisedType paramed && this.parameterisedTypeCodecs.containsKey(paramed.bareType)) {
            var codec = this.parameterisedTypeCodecs.get(paramed.bareType).apply(paramed);
            this.codecs.put(paramed, codec);
            return codec;
        }

        return res;
    }

    @Override
    public Map<VType, Codec<?>> codecsMatching(VType type) {
        return this.allTypesMatching(type).stream().map(t -> Optional.ofNullable(this.rawCodecForType(t)).map(v -> Map.entry(t, v))).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

    public Set<VType> allTypesMatching(VType type) {
        Set<VType> set = new HashSet<>();
        if (type instanceof VTemplateType template) {
            if (template.bounds == null) {
                set.addAll(this.codecs.keySet());
            } else {
                set.addAll(template.bounds.stream().map(this::allTypesMatching).flatMap(Set::stream).toList());
            }
        } else if (type instanceof VParameterisedType paramed) {
            List<Set<VType>> typesMatchingEachParam = new ArrayList<>();
            for (int i = 0; i < paramed.parameters.size(); i++) {
                typesMatchingEachParam.add(this.allTypesMatching(paramed.parameters.get(i)));
            }
            Sets.cartesianProduct(typesMatchingEachParam).forEach(assignment -> set.add(paramed.with(assignment)));
        } else {
            set.add(type);
        }

        return set;
    }

    @Override
    public Codec<VExpression> expressionCodecForType(VType type, EvaluationContext.Spec spec) {
        return this.cachedVExpressionCodecs.computeIfAbsent(new TypeAndSpecCacheKey(type, spec), $ -> new VExpressionCodec(
                new ValueCodec(type, this),
                new KeyDispatchCodec<>( //TODO holy shit
                        "function",
                        Codec.STRING,
                        (VExpression.FunctionApplication f) -> DataResult.success(f.function().name()),
                        k -> Optional.ofNullable(this.functions.get(k))
                                .map(f -> new FunctionApplicationCodec(f, this, spec))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "No function found")))
                        .codec()
                        .comapFlatMap(f -> type.contains(((VExpression.FunctionApplication) f.resolveTypes(this, spec)).resolvedSignature().outputType())
                                        ? DataResult.success(f)
                                        : DataResult.error(() -> "Unmatched type"),
                                Function.identity()),
                VariableRefCodec.CODEC));
    }

    @Override
    public void registerType(String name, VType type) {
        this.types.put(name, type);
    }

    @Override
    public void registerCodecForType(VType type, Codec<?> codec) {
        this.codecs.put(type, codec);
    }

    @Override
    public void registerCodecForParameterisedType(SimpleVType bareType, Function<VParameterisedType, Codec<?>> codecForType) {
        this.parameterisedTypeCodecs.put(bareType, codecForType);
    }

    @Override
    public Map<String, VType> allTypes() {
        return Map.copyOf(this.types);
    }

    @Override
    public void registerFunction(VFunctionDefinition function) {
        this.functions.put(function.name(), function);
    }

    @Override
    public TypeNamer createTypeNamer() {
        var reversedMap = this.types.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey));
        return new TypeNamer(reversedMap);
    }

    private record TypeAndSpecCacheKey(VType type, EvaluationContext.Spec spec) {
    }
}
