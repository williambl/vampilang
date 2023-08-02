package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.VType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FunctionApplicationCodec implements Codec<VExpression.FunctionApplication> {
    private final VFunctionDefinition functionDefinition;
    private final Function<VType, Codec<VExpression>> vTypeCodecs;

    public FunctionApplicationCodec(VFunctionDefinition functionDefinition, Function<VType, Codec<VExpression>> vTypeCodecs) {
        this.functionDefinition = functionDefinition;
        this.vTypeCodecs = vTypeCodecs; //TODO how to do this?
    }

    @Override
    public <T> DataResult<Pair<VExpression.FunctionApplication, T>> decode(DynamicOps<T> ops, T input) {
        var functionInputs = new HashMap<String, VExpression>();
        for (var functionInput : this.functionDefinition.signature().inputTypes().entrySet()) {
            var encodedInput = ops.get(input, functionInput.getKey()).result();
            if (encodedInput.isEmpty()) { //TODO optional args
                return DataResult.error(() -> "Missing required argument: " + functionInput.getKey());
            }

            var codec = this.vTypeCodecs.apply(functionInput.getValue());
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
            //TODO
        }
        return null;
    }
}
