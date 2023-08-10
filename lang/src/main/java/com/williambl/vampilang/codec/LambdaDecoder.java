package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.LambdaVType;
import com.williambl.vampilang.lang.type.VType;

import java.util.List;

public class LambdaDecoder implements Decoder<List<VExpression.Lambda>> {
    private final VType type;
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public LambdaDecoder(VType type, VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.type = type;
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    private Codec<List<VExpression>> getCodec() {
        if (!(this.type instanceof LambdaVType lambdaVType)) {
            return null;
        }

        return this.vTypeCodecs.expressionMultiCodecForType(lambdaVType.parameters.get(0), this.spec.merge(lambdaVType.specToMerge));
    }

    @Override
    public <T> DataResult<Pair<List<VExpression.Lambda>, T>> decode(DynamicOps<T> ops, T input) {
        var codec = this.getCodec();
        if (codec == null) {
            return DataResult.error(() -> "Type is not a lambda type!");
        }

        return codec.decode(ops, input)
                .map(p -> p.mapFirst(l -> l.stream().map(expr -> (VExpression.Lambda) VExpression.lambda((LambdaVType) this.type, expr)).toList()))
                .flatMap(p -> p.getFirst().isEmpty() ? DataResult.error(() -> "No lambda matches") : DataResult.success(p));
    }

    public static Codec<List<VExpression.Lambda>> createCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec, VType expectedType) {
        return Codec.of(
                new LambdaEncoder(vTypeCodecs, spec).flatComap(l -> l.stream().findFirst().map(DataResult::success).orElse(DataResult.error(() -> "No entry in list!"))),
                new LambdaDecoder(expectedType, vTypeCodecs, spec));
    }
}
