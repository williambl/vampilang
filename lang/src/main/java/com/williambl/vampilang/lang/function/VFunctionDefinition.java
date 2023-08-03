package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VValue;

import java.util.Map;
import java.util.function.Function;

public record VFunctionDefinition(String name, VFunctionSignature signature, Func function) {
    @FunctionalInterface
    public interface Func {
        public VValue apply(EvaluationContext ctx, VFunctionSignature signature, Inputs inputs);
    }

    @FunctionalInterface
    public interface Inputs {
        public VValue get(String name);
    }
}
