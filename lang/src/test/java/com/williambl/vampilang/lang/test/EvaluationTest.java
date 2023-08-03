package com.williambl.vampilang.lang.test;

import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class EvaluationTest {
    @Test
    public void correctlyEvaluatesSimpleProgram() {
        var evaluationCtx = new TypeNamer();
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.value(boolType, true),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(program::resolveTypes);
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate());
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyEvaluatesProgramWithReuseOfFunction() {
        var evaluationCtx = new TypeNamer();
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.functionApplication(ifElseFunction, Map.of(
                        "predicate", VExpression.value(boolType, true),
                        "a", VExpression.value(boolType, true),
                        "b", VExpression.value(boolType, false))),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(program::resolveTypes);
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate());
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyNamesTypesInProgramWithReuseOfFunction() {
        var evaluationCtx = new TypeNamer();
        var intType = VType.create();
        evaluationCtx.addName(intType, "int");
        var doubleType = VType.create();
        evaluationCtx.addName(doubleType, "double");
        var numType = VType.createTemplate(intType, doubleType);
        evaluationCtx.addName(numType, "number");
        var boolType = VType.create();
        evaluationCtx.addName(boolType, "bool");
        var anyType = VType.createTemplate();
        evaluationCtx.addName(anyType, "any");

        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.functionApplication(ifElseFunction, Map.of(
                        "predicate", VExpression.value(boolType, true),
                        "a", VExpression.value(boolType, true),
                        "b", VExpression.value(boolType, false))),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(program::resolveTypes);
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : type1[double|int], b : type1[double|int] -> type1[double|int]) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : type2, b : type2, predicate : bool -> type2) : a : type3, b : type3, predicate : bool -> type3)", program.toString(new TypeNamer(evaluationCtx)));
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : int, b : int -> int) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : bool, b : bool, predicate : bool -> bool) : a : int, b : int, predicate : bool -> int)", resolved.toString(new TypeNamer(evaluationCtx)));
    }
}
