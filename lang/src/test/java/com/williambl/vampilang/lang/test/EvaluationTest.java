package com.williambl.vampilang.lang.test;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationTest {
    @Test
    public void correctlyEvaluatesSimpleProgram() {
        var evaluationCtx = new EvaluationContext();
        var intType = new VType();
        var doubleType = new VType();
        var numType = new VTemplateType(Set.of(intType, doubleType));
        var boolType = new VType();
        var anyType = new VTemplateType(null);
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.value(boolType, true),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(program::resolveTypes);
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate(evaluationCtx));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyEvaluatesProgramWithReuseOfFunction() {
        var evaluationCtx = new EvaluationContext();
        var intType = new VType();
        var doubleType = new VType();
        var numType = new VTemplateType(Set.of(intType, doubleType));
        var boolType = new VType();
        var anyType = new VTemplateType(null);
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
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
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate(evaluationCtx));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyNamesTypesInProgramWithReuseOfFunction() {
        var evaluationCtx = new EvaluationContext();
        var intType = new VType();
        evaluationCtx.addName(intType, "int");
        var doubleType = new VType();
        evaluationCtx.addName(doubleType, "double");
        var numType = new VTemplateType(Set.of(intType, doubleType));
        evaluationCtx.addName(numType, "number");
        var boolType = new VType();
        evaluationCtx.addName(boolType, "bool");
        var anyType = new VTemplateType(null);
        evaluationCtx.addName(anyType, "any");

        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
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
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : type1[double|int], b : type1[double|int] -> type1[double|int]) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : type2, b : type2, predicate : bool -> type2) : a : type3, b : type3, predicate : bool -> type3)", program.toString(new EvaluationContext(evaluationCtx)));
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : int, b : int -> int) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : bool, b : bool, predicate : bool -> bool) : a : int, b : int, predicate : bool -> int)", resolved.toString(new EvaluationContext(evaluationCtx)));
    }
}
