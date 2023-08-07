package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;

import java.util.HashMap;
import java.util.Map;

public class FunctionApplicationCodec implements Codec<VExpression.FunctionApplication> {
    private final VFunctionDefinition functionDefinition;
    private final VEnvironment vTypeCodecs;
    private EvaluationContext.Spec spec;

    public FunctionApplicationCodec(VFunctionDefinition functionDefinition, VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.functionDefinition = functionDefinition;
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<Pair<VExpression.FunctionApplication, T>> decode(DynamicOps<T> ops, T input) {
        var functionInputs = new HashMap<String, VExpression>();
        for (var functionInput : this.functionDefinition.signature().inputTypes().entrySet()) {
            var encodedInput = ops.get(input, functionInput.getKey()).result();
            if (encodedInput.isEmpty()) { //TODO optional args
                return DataResult.error(() -> "Missing required argument: " + functionInput.getKey());
            }

            var codec = this.vTypeCodecs.expressionCodecForType(functionInput.getValue(), this.spec);
            var decodedInput = codec.decode(ops, encodedInput.get());
            var error = decodedInput.error();
            if (error.isPresent()) {
                return DataResult.error(() -> "Error decoding argument %s: %s".formatted(functionInput.getKey(), error.get().message()));
            }

            functionInputs.put(functionInput.getKey(), decodedInput.getOrThrow(false, $ -> {}).getFirst());
        }

        return DataResult.success(Pair.of((VExpression.FunctionApplication) VExpression.functionApplication(this.functionDefinition, Map.copyOf(functionInputs)), input));
    }

    @Override
    public <T> DataResult<T> encode(VExpression.FunctionApplication input, DynamicOps<T> ops, T prefix) {
        for (var functionInput : input.inputs().entrySet()) {
            var encodedInput = this.vTypeCodecs.expressionCodecForType(functionInput.getValue().type(), this.spec).encodeStart(ops, functionInput.getValue()).result();
            if (encodedInput.isEmpty()) {
                return DataResult.error(() -> "Could not encode argument: " + functionInput.getKey());
            }

            prefix = ops.set(prefix, functionInput.getKey(), encodedInput.get());
        }

        return DataResult.success(prefix);
    }
}
