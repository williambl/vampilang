package com.williambl.vampilang.codec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;

public class ObjectConstructionEncoder implements Encoder<VExpression.ObjectConstruction> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public ObjectConstructionEncoder(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<T> encode(VExpression.ObjectConstruction input, DynamicOps<T> ops, T prefix) {
        prefix = ops.set(prefix, "v-type", ops.createString(input.typeName()));
        for (var property : input.properties().entrySet()) {
            var encodedInput = this.vTypeCodecs.expressionCodecForType(property.getValue().type(), this.spec).encodeStart(ops, property.getValue()).result();
            if (encodedInput.isEmpty()) {
                return DataResult.error(() -> "Could not encode property: " + property.getKey());
            }

            prefix = ops.set(prefix, property.getKey(), encodedInput.get());
        }

        return DataResult.success(prefix);
    }
}
