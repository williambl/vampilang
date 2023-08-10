package com.williambl.vampilang.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;

public class LambdaEncoder implements Encoder<VExpression.Lambda> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public LambdaEncoder(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<T> encode(VExpression.Lambda input, DynamicOps<T> ops, T prefix) {
        Encoder<VExpression> encoder = this.vTypeCodecs.expressionCodecForType(input.type().parameters.get(0), this.spec.merge(input.type().specToMerge()));
        return encoder.encode(input.expr(), ops, prefix);
    }
}
