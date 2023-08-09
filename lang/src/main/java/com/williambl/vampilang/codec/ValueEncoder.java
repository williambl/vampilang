package com.williambl.vampilang.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;

public class ValueEncoder implements Encoder<VExpression.Value> {
    private final VEnvironment vTypeCodecs;

    public ValueEncoder(VEnvironment vTypeCodecs) {
        this.vTypeCodecs = vTypeCodecs;
    }

    @Override
    public <T> DataResult<T> encode(VExpression.Value input, DynamicOps<T> ops, T prefix) {
        @SuppressWarnings("unchecked") Encoder<Object> encoder = (Encoder<Object>) this.vTypeCodecs.rawCodecForType(input.value().type());
        return encoder.encode(input.value().value(), ops, prefix);
    }
}
