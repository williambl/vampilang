package com.williambl.vampilang.codec;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionApplicationDecoder implements Decoder<List<VExpression.FunctionApplication>> {
    private final VFunctionDefinition functionDefinition;
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public FunctionApplicationDecoder(VFunctionDefinition functionDefinition, VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.functionDefinition = functionDefinition;
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<Pair<List<VExpression.FunctionApplication>, T>> decode(DynamicOps<T> ops, T input) {
        var functionInputs = new HashMap<String, List<VExpression>>();
        for (var functionInput : this.functionDefinition.signature().inputTypes().entrySet()) {
            var encodedInput = ops.get(input, functionInput.getKey()).result();
            if (encodedInput.isEmpty()) { //TODO optional args
                return DataResult.error(() -> "Missing required argument: " + functionInput.getKey());
            }

            var codec = this.vTypeCodecs.expressionMultiCodecForType(functionInput.getValue(), this.spec);
            var decodedInput = codec.decode(ops, encodedInput.get());
            var error = decodedInput.error();
            if (error.isPresent()) {
                return DataResult.error(() -> "Error decoding argument %s: %s".formatted(functionInput.getKey(), error.get().message()));
            }

            functionInputs.computeIfAbsent(functionInput.getKey(), $ -> new ArrayList<>()).addAll(decodedInput.getOrThrow(IllegalStateException::new).getFirst());
        }

        var res = Sets.cartesianProduct(
                functionInputs.entrySet()
                        .stream()
                        .map(kv -> kv.getValue().stream().map(v -> Map.entry(kv.getKey(), v)).collect(Collectors.toSet()))
                        .toList()).stream()
                .map(entries -> (VExpression.FunctionApplication) VExpression.functionApplication(this.functionDefinition, Map.ofEntries(entries.toArray(new Map.Entry[0]))))
                .toList();

        return DataResult.success(Pair.of(res, input));
    }

    public static Codec<List<VExpression.FunctionApplication>> createCodec(VFunctionDefinition functionDefinition, VEnvironment env, EvaluationContext.Spec spec) {
        return Codec.of(new FunctionApplicationEncoder(env, spec).flatComap(l -> l.stream().findFirst().map(DataResult::success).orElse(DataResult.error(() -> "No entry in list!"))), new FunctionApplicationDecoder(functionDefinition, env, spec));
    }
}
