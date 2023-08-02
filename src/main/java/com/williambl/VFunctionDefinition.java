package com.williambl;

import java.util.List;
import java.util.function.BiFunction;

public record VFunctionDefinition(String name, VFunctionSignature signature, BiFunction<VFunctionSignature, List<VValue>, VValue> function) {
}
