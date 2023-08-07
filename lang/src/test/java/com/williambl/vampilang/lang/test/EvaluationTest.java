package com.williambl.vampilang.lang.test;

import com.williambl.vampilang.lang.*;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class EvaluationTest {
    @Test
    public void correctlyEvaluatesSimpleProgram() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var env = new VEnvironmentImpl();
        env.registerType("int", intType);
        env.registerType("double", doubleType);
        env.registerType("number", numType);
        env.registerType("bool", boolType);
        env.registerType("any", anyType);
        env.registerFunction(addFunction);
        env.registerFunction(ifElseFunction);
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.value(boolType, true),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(() -> program.resolveTypes(env, new EvaluationContext.Spec()));
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate(new EvaluationContext()));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyEvaluatesProgramWithReuseOfFunction() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var env = new VEnvironmentImpl();
        env.registerType("int", intType);
        env.registerType("double", doubleType);
        env.registerType("number", numType);
        env.registerType("bool", boolType);
        env.registerType("any", anyType);
        env.registerFunction(addFunction);
        env.registerFunction(ifElseFunction);
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.functionApplication(ifElseFunction, Map.of(
                        "predicate", VExpression.value(boolType, true),
                        "a", VExpression.value(boolType, true),
                        "b", VExpression.value(boolType, false))),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(() -> program.resolveTypes(env, new EvaluationContext.Spec()));
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate(new EvaluationContext()));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyNamesTypesInProgramWithReuseOfFunction() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var env = new VEnvironmentImpl();
        env.registerType("int", intType);
        env.registerType("double", doubleType);
        env.registerType("number", numType);
        env.registerType("bool", boolType);
        env.registerType("any", anyType);
        env.registerFunction(addFunction);
        env.registerFunction(ifElseFunction);
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.functionApplication(ifElseFunction, Map.of(
                        "predicate", VExpression.value(boolType, true),
                        "a", VExpression.value(boolType, true),
                        "b", VExpression.value(boolType, false))),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.value(intType, 5),
                        "b", VExpression.value(intType, 10))),
                "b", VExpression.value(intType, 25)));
        var resolved = Assertions.assertDoesNotThrow(() -> program.resolveTypes(env, new EvaluationContext.Spec()));
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : type1[double|int], b : type1[double|int] -> type1[double|int]) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : type2, b : type2, predicate : bool -> type2) : a : type3, b : type3, predicate : bool -> type3)", program.toString(env.createTypeNamer()));
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : int, b : int -> int) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : bool, b : bool, predicate : bool -> bool) : a : int, b : int, predicate : bool -> int)", resolved.toString(env.createTypeNamer()));
    }

    @Test
    public void correctlyEvaluatesProgramWithVariables() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value()));
        var evaluationSpec = new EvaluationContext.Spec(Map.of("var1", intType, "var2", intType));
        var env = new VEnvironmentImpl();
        env.registerType("int", intType);
        env.registerType("double", doubleType);
        env.registerType("number", numType);
        env.registerType("bool", boolType);
        env.registerType("any", anyType);
        env.registerFunction(addFunction);
        env.registerFunction(ifElseFunction);
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.value(boolType, true),
                "a", VExpression.functionApplication(addFunction, Map.of(
                        "a", VExpression.variable("var1"),
                        "b", VExpression.variable("var2"))),
                "b", VExpression.variable("var1")));
        var resolved = Assertions.assertDoesNotThrow(() -> program.resolveTypes(env, evaluationSpec));
        var result = Assertions.assertDoesNotThrow(() -> resolved.evaluate(EvaluationContext.builder(evaluationSpec).addVariable("var1", new VValue(intType, 5)).addVariable("var2", new VValue(intType, 10)).build()));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }
}
