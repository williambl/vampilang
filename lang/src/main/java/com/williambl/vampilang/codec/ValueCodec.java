package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.VType;

import java.util.Map;

public class ValueCodec implements Codec<VExpression.Value> {
    private final VType type;
    private final VTypeCodecRegistry vTypeCodecs;

    public ValueCodec(VType type, VTypeCodecRegistry vTypeCodecs) {
        this.type = type;
        this.vTypeCodecs = vTypeCodecs;
    }

    private Map<VType, ? extends Codec<?>> getCodecs() {
        return this.vTypeCodecs.codecsMatching(this.type);
    }

    @Override
    public <T> DataResult<Pair<VExpression.Value, T>> decode(DynamicOps<T> ops, T input) {
        var codecs = this.getCodecs();
        for (var kv : codecs.entrySet()) {
            var res = kv.getValue().decode(ops, input).map(p -> p.mapFirst(v -> (VExpression.Value) VExpression.value(kv.getKey(), v))).result();
            if (res.isPresent()) {
                return DataResult.success(res.get());
            }
        }

        return DataResult.error(() -> "No matching type");
    }

    @Override
    public <T> DataResult<T> encode(VExpression.Value input, DynamicOps<T> ops, T prefix) {
        @SuppressWarnings("unchecked") Encoder<Object> encoder = (Encoder<Object>) this.vTypeCodecs.rawCodecForType(input.value().type());
        return encoder.encode(input.value().value(), ops, prefix);
    }
}
