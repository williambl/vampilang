package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.williambl.vampilang.lang.VExpression;

import java.util.ArrayList;
import java.util.List;

public class VExpressionCodec implements Codec<List<VExpression>> {
    private final Codec<List<VExpression.Value>> valueCodec;
    private final Codec<List<VExpression.FunctionApplication>> functionApplicationCodec;
    private final Codec<VExpression.VariableRef> variableRefCodec;
    private final Codec<List<VExpression.ObjectConstruction>> objectConstructionCodec;
    private final Codec<List<VExpression.ListConstruction>> listConstructionCodec;

    public VExpressionCodec(Codec<List<VExpression.Value>> valueCodec, Codec<List<VExpression.FunctionApplication>> functionApplicationCodec, Codec<VExpression.VariableRef> variableRefCodec, Codec<List<VExpression.ObjectConstruction>> objectConstructionCodec, Codec<List<VExpression.ListConstruction>> listConstructionCodec) {
        this.valueCodec = valueCodec;
        this.functionApplicationCodec = functionApplicationCodec;
        this.variableRefCodec = variableRefCodec;
        this.objectConstructionCodec = objectConstructionCodec;
        this.listConstructionCodec = listConstructionCodec;
    }

    @Override
    public <T> DataResult<Pair<List<VExpression>, T>> decode(DynamicOps<T> ops, T input) {
        var results = new ArrayList<VExpression>();
        var variableRefRead = this.variableRefCodec.decode(ops, input).map(p -> p.mapFirst(VExpression.class::cast));
        if (variableRefRead.result().isPresent()) {
            results.add(variableRefRead.result().get().getFirst());
        }

        var functionApplicationRead = this.functionApplicationCodec.decode(ops, input).map(p -> p.mapFirst(l -> l.stream().map(VExpression.class::cast).toList()));
        if (functionApplicationRead.result().isPresent()) {
            results.addAll(functionApplicationRead.result().get().getFirst());
        }

        var listRead = this.listConstructionCodec.decode(ops, input).map(p -> p.mapFirst(l -> l.stream().map(VExpression.class::cast).toList()));
        if (listRead.result().isPresent()) {
            results.addAll(listRead.result().get().getFirst());
        }

        var objectRead = this.objectConstructionCodec.decode(ops, input).map(p -> p.mapFirst(l -> l.stream().map(VExpression.class::cast).toList()));
        if (objectRead.result().isPresent()) {
            results.addAll(objectRead.result().get().getFirst());
        }

        var valueRead = this.valueCodec.decode(ops, input).map(p -> p.mapFirst(l -> l.stream().map(VExpression.class::cast).toList()));
        if (valueRead.result().isPresent()) {
            results.addAll(valueRead.result().get().getFirst());
        }

        if (results.isEmpty()) {
            return DataResult.error(() -> "Not a valid variable ref, value, list, object, or function application. Error from each:\nvariable ref: %s,\nvalue: %s,\nlist: %s,\nobject: %s,\nfunction application: %s".formatted(
                    variableRefRead.error().map(DataResult.PartialResult::message).orElse("?"),
                    valueRead.error().map(DataResult.PartialResult::message).orElse("?"),
                    listRead.error().map(DataResult.PartialResult::message).orElse("?"),
                    objectRead.error().map(DataResult.PartialResult::message).orElse("?"),
                    functionApplicationRead.error().map(DataResult.PartialResult::message).orElse("?")));
        }

        return DataResult.success(Pair.of(results, input));
    }

    @Override
    public <T> DataResult<T> encode(List<VExpression> inputs, DynamicOps<T> ops, T prefix) {
        if (inputs.isEmpty()) {
            return DataResult.error(() -> "No input!");
        }

        var input = inputs.get(0);
        if (input instanceof VExpression.FunctionApplication funcInput) {
            return this.functionApplicationCodec.encode(List.of(funcInput), ops, prefix);
        } else if (input instanceof VExpression.Value valueInput) {
            return this.valueCodec.encode(List.of(valueInput), ops, prefix);
        } else if (input instanceof VExpression.VariableRef varRefInput) {
            return this.variableRefCodec.encode(varRefInput, ops, prefix);
        } else if (input instanceof VExpression.ObjectConstruction objectInput) {
            return this.objectConstructionCodec.encode(List.of(objectInput), ops, prefix);
        } else if (input instanceof VExpression.ListConstruction listInput) {
            return this.listConstructionCodec.encode(List.of(listInput), ops, prefix);
        }

        return DataResult.error(() -> "Expected VExpression to be one of FunctionApplication, Value, or VariableRef!");
    }
}
