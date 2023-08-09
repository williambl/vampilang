package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;

import java.util.List;

public class ListConstructionDecoder implements Decoder<List<VExpression.ListConstruction>> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;
    private final VParameterisedType expectedType;

    public ListConstructionDecoder(VEnvironment vTypeCodecs, EvaluationContext.Spec spec, VType expectedType) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
        this.expectedType = expectedType instanceof VParameterisedType paramed ? paramed : null;
    }

    @Override
    public <T> DataResult<Pair<List<VExpression.ListConstruction>, T>> decode(DynamicOps<T> ops, T input) {
        if (this.expectedType != null) {
            var listCodec = this.vTypeCodecs.expressionMultiCodecForType(this.expectedType.parameters.get(0), this.spec).listOf() //TODO cache
                    .xmap(l -> l.stream().map(entries -> (VExpression.ListConstruction) VExpression.list(entries)).toList(), l -> l.stream().map(VExpression.ListConstruction::entries).toList());
            return listCodec.decode(ops, input);
        } else {
            return DataResult.error(() -> "No types match");
        }
    }

    public static Codec<List<VExpression.ListConstruction>> createCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec, VType expectedType) {
        return Codec.of(
                new ListConstructionEncoder(vTypeCodecs, spec, expectedType).flatComap(l -> l.stream().findFirst().map(DataResult::success).orElse(DataResult.error(() -> "No entry in list!"))),
                new ListConstructionDecoder(vTypeCodecs, spec, expectedType));
    }
}
