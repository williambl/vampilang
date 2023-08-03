package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.VValue;

import java.util.Map;

public record VFunctionDefinition(String name, VFunctionSignature signature, Func function) {
    @FunctionalInterface
    public interface Func {
        public VValue apply(VFunctionSignature signature, Map<String, VValue> inputs);
    }
}
