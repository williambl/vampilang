package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VValue;

import java.util.List;
import java.util.function.BiFunction;

public record VFunctionDefinition(String name, VFunctionSignature signature, Func function) {
    @FunctionalInterface
    public interface Func {
        public VValue apply(EvaluationContext ctx, VFunctionSignature signature, List<VValue> inputs);
    }
}
