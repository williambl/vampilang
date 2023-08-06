package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.VType;

public class VExpressionCodec implements Codec<VExpression> {
    private final Codec<VExpression.Value> valueCodec;
    private final Codec<VExpression.FunctionApplication> functionApplicationCodec;
    private final Codec<VExpression.VariableRef> variableRefCodec;

    public VExpressionCodec(Codec<VExpression.Value> valueCodec, Codec<VExpression.FunctionApplication> functionApplicationCodec, Codec<VExpression.VariableRef> variableRefCodec) {
        this.valueCodec = valueCodec;
        this.functionApplicationCodec = functionApplicationCodec;
        this.variableRefCodec = variableRefCodec;
    }

    @Override
    public <T> DataResult<Pair<VExpression, T>> decode(DynamicOps<T> ops, T input) {
        var variableRefRead = this.variableRefCodec.decode(ops, input).map(p -> p.mapFirst(VExpression.class::cast));
        if (variableRefRead.result().isPresent()) {
            return variableRefRead;
        }

        var valueRead = this.valueCodec.decode(ops, input).map(p -> p.mapFirst(VExpression.class::cast));
        if (valueRead.result().isPresent()) {
            return valueRead;
        }

        var functionApplicationRead = this.functionApplicationCodec.decode(ops, input).map(p -> p.mapFirst(VExpression.class::cast));
        if (functionApplicationRead.result().isPresent()) {
            return functionApplicationRead;
        }

        return DataResult.error(() -> "Not a valid variable ref, value, or function application. Error from each:\nvariable ref: %s,\nvalue: %s,\nfunction application: %s".formatted(
                variableRefRead.error().map(DataResult.PartialResult::message).orElse("?"),
                valueRead.error().map(DataResult.PartialResult::message).orElse("?"),
                functionApplicationRead.error().map(DataResult.PartialResult::message).orElse("?")));
    }

    @Override
    public <T> DataResult<T> encode(VExpression input, DynamicOps<T> ops, T prefix) {
        if (input instanceof VExpression.FunctionApplication funcInput) {
            return this.functionApplicationCodec.encode(funcInput, ops, prefix);
        } else if (input instanceof VExpression.Value valueInput) {
            return this.valueCodec.encode(valueInput, ops, prefix);
        } else if (input instanceof VExpression.VariableRef varRefInput) {
            return this.variableRefCodec.encode(varRefInput, ops, prefix);
        }

        throw new IncompatibleClassChangeError("Expected VExpression to be one of FunctionApplication, Value, or VariableRef!");
    }
}
