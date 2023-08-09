package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.VType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValueDecoder implements Decoder<List<VExpression.Value>> {
    private final VType type;
    private final VEnvironment vTypeCodecs;

    public ValueDecoder(VType type, VEnvironment vTypeCodecs) {
        this.type = type;
        this.vTypeCodecs = vTypeCodecs;
    }

    private Map<VType, ? extends Codec<?>> getCodecs() {
        return this.vTypeCodecs.codecsMatching(this.type);
    }

    @Override
    public <T> DataResult<Pair<List<VExpression.Value>, T>> decode(DynamicOps<T> ops, T input) {
        var codecs = this.getCodecs();
        var results = new ArrayList<VExpression.Value>();
        for (var kv : codecs.entrySet()) {
            var res = kv.getValue().decode(ops, input).map(p -> p.mapFirst(v -> (VExpression.Value) VExpression.value(kv.getKey(), v))).result();
            res.ifPresent(pair -> results.add(pair.getFirst()));
        }

        if (results.isEmpty()) {
            return DataResult.error(() -> "No matching type");
        } else {
            return DataResult.success(Pair.of(results, input));
        }
    }

    public static Codec<List<VExpression.Value>> createCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec, VType expectedType) {
        return Codec.of(
                new ValueEncoder(vTypeCodecs).flatComap(l -> l.stream().findFirst().map(DataResult::success).orElse(DataResult.error(() -> "No entry in list!"))),
                new ValueDecoder(expectedType, vTypeCodecs));
    }
}
