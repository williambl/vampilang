package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.ConstructableVType;

import java.util.HashMap;
import java.util.Map;

public class ObjectConstructionCodec implements Codec<VExpression.ObjectConstruction> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public ObjectConstructionCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<Pair<VExpression.ObjectConstruction, T>> decode(DynamicOps<T> ops, T input) {
        var properties = new HashMap<String, VExpression>();
        var typeName = ops.get(input, "v-type").flatMap(t -> Codec.STRING.decode(ops, t).map(Pair::getFirst)).result();
        if (typeName.isEmpty()) {
            return DataResult.error(() -> "No type supplied (key 'v-type')");
        }

        var typeOpt = typeName.map(this.vTypeCodecs::getType).map(t -> t instanceof ConstructableVType<?> c ? c : null);
        if (typeOpt.isEmpty()) {
            return DataResult.error(() -> "No such type %s".formatted(typeName));
        }

        for (var property : typeOpt.get().propertyTypes.entrySet()) {
            var encodedProperty = ops.get(input, property.getKey()).result();
            if (encodedProperty.isEmpty()) { //TODO optional properties
                return DataResult.error(() -> "Missing required property: " + property.getKey());
            }

            var codec = this.vTypeCodecs.expressionCodecForType(property.getValue(), this.spec);
            var decodedInput = codec.decode(ops, encodedProperty.get());
            var error = decodedInput.error();
            if (error.isPresent()) {
                return DataResult.error(() -> "Error decoding property %s: %s".formatted(property.getKey(), error.get().message()));
            }

            properties.put(property.getKey(), decodedInput.getOrThrow(false, $ -> {}).getFirst());
        }

        return DataResult.success(Pair.of((VExpression.ObjectConstruction) VExpression.object(typeName.get(), Map.copyOf(properties)), input));
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
