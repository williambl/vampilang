package com.williambl.vampilang.codec;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
            Codec<List<VExpression.ListConstruction>> listCodec = this.vTypeCodecs.expressionMultiCodecForType(this.expectedType.parameters.get(0), this.spec) // this codec produces a list of possible expressions
                    .xmap(HashSet::new, ArrayList::new) // transform it into a set of possible expressions (so we can do cartesian product)
                    .listOf() // transform it into a list of possible sets of expressions, one set for each list entry
                    .comapFlatMap(possibilitiesForEntries -> {
                        if (possibilitiesForEntries.isEmpty()) {
                            return DataResult.error(() -> "No elements match");
                        }
                        var cartesianProduct = Sets.cartesianProduct(possibilitiesForEntries); // get the cartesian product of all the possibilities (all possible lists)
                        return DataResult.success(cartesianProduct.stream().map(s -> (VExpression.ListConstruction) VExpression.list(s)).toList()); // transform it into a list of possible list expressions
                    }, list -> list.stream().map(VExpression.ListConstruction::entries).map(HashSet::new).toList());


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
