package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.ConstructableVType;
import com.williambl.vampilang.lang.type.VType;

import java.util.HashMap;
import java.util.Map;

public class ListConstructionCodec implements Codec<VExpression.ListConstruction> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public ListConstructionCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<Pair<VExpression.ListConstruction, T>> decode(DynamicOps<T> ops, T input) {
        var listCodec = this.vTypeCodecs.expressionCodecForType(VType.createTemplate(), this.spec).listOf() //TODO cache
                .xmap(l -> (VExpression.ListConstruction) VExpression.list(l), VExpression.ListConstruction::entries);
        return listCodec.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(VExpression.ListConstruction input, DynamicOps<T> ops, T prefix) {
        var listCodec = this.vTypeCodecs.expressionCodecForType(VType.createTemplate(), this.spec).listOf()
                .xmap(l -> (VExpression.ListConstruction) VExpression.list(l), VExpression.ListConstruction::entries);
        return listCodec.encode(input, ops, prefix);
    }
}
