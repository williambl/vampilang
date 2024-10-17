package com.williambl.vampilang.lang;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.williambl.vampilang.codec.*;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VEnvironmentImpl implements VEnvironment {
    protected final Map<String, VType> types = new HashMap<>();
    protected final Map<VType, Codec<?>> codecs = new HashMap<>();
    protected final Map<VType, Function<VParameterisedType, Codec<?>>> parameterisedTypeCodecs = new HashMap<>();
    protected final Map<String, VFunctionDefinition> functions = new HashMap<>();
    protected final Map<TypeAndSpecCacheKey, Codec<VExpression>> cachedVExpressionCodecs = new HashMap<>();
    protected final Map<TypeAndSpecCacheKey, Codec<List<VExpression>>> cachedVExpressionMultiCodecs = new HashMap<>();

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
        if (type instanceof VTopTemplateType) {
            set.addAll(this.codecs.keySet());
        } else if (type instanceof VFixedTemplateType template) {
            set.addAll(template.bounds.stream().map(this::allTypesMatching).flatMap(Set::stream).toList());
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
        return this.cachedVExpressionCodecs.computeIfAbsent(new TypeAndSpecCacheKey(type, spec), $ ->
                this.expressionMultiCodecForType(type, spec).comapFlatMap(
                        exprs -> exprs.stream()
                                .map(expr -> expr.resolveTypes(this, spec))
                                .map(DataResult::result)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(expr -> type.contains(expr.type(), this))
                                .map(DataResult::success)
                                .findFirst()
                                .orElse(DataResult.error(() -> "Unmatched type")),
                        List::of)
        );
    }

    @Override
    public Codec<List<VExpression>> expressionMultiCodecForType(VType type, EvaluationContext.Spec spec) {
        return this.cachedVExpressionMultiCodecs.computeIfAbsent(new TypeAndSpecCacheKey(type, spec), $ -> new VExpressionCodec(
                ValueDecoder.createCodec(this, spec, type),
                KeyDispatchCodec.<VFunctionDefinition, List<VExpression.FunctionApplication>>unsafe("function", Codec.STRING.comapFlatMap(
                                        name -> Optional.ofNullable(this.functions.get(name)).map(DataResult::success).orElse(DataResult.error(() -> "No such function with name "+name)),
                                        VFunctionDefinition::name),
                                exprs -> exprs.stream().map(expr -> DataResult.success(expr.function())).findFirst().orElse(DataResult.error(() -> "No entry in list!")),
                                func -> DataResult.success(FunctionApplicationDecoder.createCodec(func, this, spec)),
                                funcs -> funcs.stream().map(func -> DataResult.success(FunctionApplicationDecoder.createCodec(func.function(), this, spec))).findFirst().orElse(DataResult.error(() -> "No entry in list!"))).codec()
                        .comapFlatMap(fs -> Optional.of(fs.stream()
                                                .map(f -> f.resolveTypes(this, spec)
                                                        .flatMap(fr -> type.contains(((VExpression.FunctionApplication)fr).resolvedSignature().outputType(), this)
                                                                ? DataResult.success((VExpression.FunctionApplication) fr)
                                                                : DataResult.error(() -> "Unmatched type")))
                                                .map(DataResult::result)
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .toList())
                                        .filter(l -> !l.isEmpty())
                                        .map(DataResult::success)
                                        .orElse(DataResult.error(() -> "Unmatched type")),
                                Function.identity()),
                VariableRefCodec.CODEC,
                ObjectConstructionDecoder.createCodec(this, spec).comapFlatMap(os ->
                        Optional.of(os.stream()
                                .map(o -> o.resolveTypes(this, spec)
                                        .flatMap(or -> type.contains(or.type(), this)
                                                ? DataResult.success((VExpression.ObjectConstruction) or)
                                                : DataResult.error(() -> "Unmatched type")))
                                        .map(DataResult::result)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .toList())
                                .filter(l -> !l.isEmpty())
                                .map(DataResult::success)
                                .orElse(DataResult.error(() -> "Unmatched type!")),
                        Function.identity()),
                ListConstructionDecoder.createCodec(this, spec, type),
                LambdaDecoder.createCodec(this, spec, type)));
    }

    @Override
    public void registerType(String name, VType type) {
        this.types.put(name, type);
    }

    @Override
    public VType getType(String typeName) {
        return this.types.get(typeName);
    }

    @Override
    public VParameterisedType listType() {
        var type = this.getType("list");
        return type == null ? null : VType.createParameterised(type, VType.createTopTemplate());
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

    protected record TypeAndSpecCacheKey(VType type, EvaluationContext.Spec spec) {
    }
}
