package com.williambl.vampilang.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;

public class FunctionApplicationEncoder implements Encoder<VExpression.FunctionApplication> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public FunctionApplicationEncoder(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
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
