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
        var boolType = new VType();
        typeNames.put(boolType, "bool");
        var anyType = new VTemplateType(null);
        typeNames.put(anyType, "any");

        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(List.of(numType, numType), numType), (sig, a) -> new VValue(sig.outputType(), ((Number) a.get(0).value()).doubleValue() + ((Number) a.get(1).value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(List.of(boolType, anyType, anyType), anyType), (sig, a) -> new VValue(sig.outputType(), (boolean) a.get(0).value() ? a.get(1).value() : a.get(2).value()));
        var program = VExpression.functionApplication(ifElseFunction,
                VExpression.value(boolType, true),
                VExpression.functionApplication(addFunction,
                        VExpression.value(intType, 5),
                        VExpression.value(intType, 10)),
                VExpression.value(intType, 25)
        );
        System.out.println(program.toString(typeNames));
        var resolvedProgram = program.resolveTypes();
        System.out.println(resolvedProgram.toString(typeNames));
        System.out.println(resolvedProgram.evaluate());
    }
}