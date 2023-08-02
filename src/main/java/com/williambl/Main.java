package com.williambl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Map<VType, String> typeNames = new HashMap<>();
        var intType = new VType();
        typeNames.put(intType, "int");
        var doubleType = new VType();
        typeNames.put(doubleType, "double");
        var numType = new VTemplateType(Set.of(intType, doubleType));
        typeNames.put(numType, "number");

        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(List.of(numType, numType), numType), (sig, a) -> new VValue<>(sig.outputType(), (Double) a.get(0).value() + (Double) a.get(1).value()));
        List<VValue<?>> functionInputs = List.of(
                new VValue<>(doubleType, 2.0),
                new VValue<>(doubleType, 2.0)
        );
        System.out.println(addFunction.signature().toString(typeNames));
        var resolvedSignature = addFunction.signature().resolveTypes(functionInputs.stream().map(VValue::type).toList());
        System.out.println(resolvedSignature.toString(typeNames));
        System.out.println(addFunction.function().apply(resolvedSignature, functionInputs).value());
    }
}