package com.williambl.vampilang.codec;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.ConstructableVType;
import com.williambl.vampilang.lang.type.VType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObjectConstructionDecoder implements Decoder<List<VExpression.ObjectConstruction>> {
    private final VEnvironment vTypeCodecs;
    private final EvaluationContext.Spec spec;

    public ObjectConstructionDecoder(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        this.vTypeCodecs = vTypeCodecs;
        this.spec = spec;
    }

    @Override
    public <T> DataResult<Pair<List<VExpression.ObjectConstruction>, T>> decode(DynamicOps<T> ops, T input) {
        var properties = new HashMap<String, List<VExpression>>();
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

            properties.computeIfAbsent(property.getKey(), $ -> new ArrayList<>()).add(decodedInput.getOrThrow(IllegalStateException::new).getFirst());
        }

        var res = Sets.cartesianProduct(
                        properties.entrySet()
                                .stream()
                                .map(kv -> kv.getValue().stream().map(v -> Map.entry(kv.getKey(), v)).collect(Collectors.toSet()))
                                .toList()).stream()
                .map(entries -> (VExpression.ObjectConstruction) VExpression.object(typeName.get(), Map.ofEntries(entries.toArray(new Map.Entry[0]))))
                .toList();

        return DataResult.success(Pair.of(res, input));
    }

    public static Codec<List<VExpression.ObjectConstruction>> createCodec(VEnvironment vTypeCodecs, EvaluationContext.Spec spec) {
        return Codec.of(
                new ObjectConstructionEncoder(vTypeCodecs, spec).flatComap(l -> l.stream().findFirst().map(DataResult::success).orElse(DataResult.error(() -> "No entry in list!"))),
                new ObjectConstructionDecoder(vTypeCodecs, spec));
    }
}
