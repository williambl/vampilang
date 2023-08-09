package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;

public class ListConstructionCodec implements Codec<VExpression.ListConstruction> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;
    private final VParameterisedType expectedType;

    public ListConstructionCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec, VType expectedType) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
        this.expectedType = expectedType instanceof VParameterisedType paramed ? paramed : null;
    }

    @Override
    public <T> DataResult<Pair<VExpression.ListConstruction, T>> decode(DynamicOps<T> ops, T input) {
        if (this.expectedType != null) {
            var listCodec = this.vTypeCodecs.expressionCodecForType(this.expectedType.parameters.get(0), this.spec).listOf() //TODO cache
                    .xmap(l -> (VExpression.ListConstruction) VExpression.list(l), VExpression.ListConstruction::entries);
            return listCodec.decode(ops, input);
        } else {
            return DataResult.error(() -> "No types match");
        }
    }

    @Override
    public <T> DataResult<T> encode(VExpression.ListConstruction input, DynamicOps<T> ops, T prefix) {
        if (this.expectedType != null) {
            var listCodec = this.vTypeCodecs.expressionCodecForType(this.expectedType.parameters.get(0), this.spec).listOf()
                    .xmap(l -> (VExpression.ListConstruction) VExpression.list(l), VExpression.ListConstruction::entries);
            return listCodec.encode(input, ops, prefix);
        } else {
            return DataResult.error(() -> "No types match");
        }
    }
}
