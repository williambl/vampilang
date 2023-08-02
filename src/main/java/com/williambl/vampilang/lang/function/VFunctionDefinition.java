package com.williambl.vampilang.lang.function;

import com.williambl.vampilang.lang.VValue;

import java.util.List;
import java.util.function.BiFunction;

public record VFunctionDefinition(String name, VFunctionSignature signature, BiFunction<VFunctionSignature, List<VValue>, VValue> function) {
}
