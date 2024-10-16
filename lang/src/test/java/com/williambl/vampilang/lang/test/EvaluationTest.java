package com.williambl.vampilang.lang.test;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.*;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EvaluationTest {
    @Test
    public void correctlyEvaluatesSimpleProgram() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTopTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> VValue.value(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue(), ctx.env()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> VValue.value(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value(), ctx.env()));
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
        var resolved = program.resolveTypes(env, new EvaluationContext.Spec()).result();
        Assertions.assertTrue(resolved.isPresent());
        var result = Assertions.assertDoesNotThrow(() -> resolved.get().evaluate(new EvaluationContext(env)));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyEvaluatesProgramWithReuseOfFunction() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTopTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> VValue.value(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue(), ctx.env()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> VValue.value(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value(), ctx.env()));
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
        var resolved = program.resolveTypes(env, new EvaluationContext.Spec()).result();
        Assertions.assertTrue(resolved.isPresent());
        var result = Assertions.assertDoesNotThrow(() -> resolved.get().evaluate(new EvaluationContext(env)));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyNamesTypesInProgramWithReuseOfFunction() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTopTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> VValue.value(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue(), ctx.env()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> VValue.value(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value(), ctx.env()));
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
        var resolved = program.resolveTypes(env, new EvaluationContext.Spec()).result();
        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : type1[double|int], b : type1[double|int] -> type1[double|int]) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : type2[all], b : type2[all], predicate : bool -> type2[all]) : a : type3[all], b : type3[all], predicate : bool -> type3[all])", program.toString(env.createTypeNamer()));
        Assertions.assertEquals("(function if-else a = (function add a = (value 5 : int) b = (value 10 : int) : a : int, b : int -> int) b = (value 25 : int) predicate = (function if-else a = (value true : bool) b = (value false : bool) predicate = (value true : bool) : a : bool, b : bool, predicate : bool -> bool) : a : int, b : int, predicate : bool -> int)", resolved.get().toString(env.createTypeNamer()));
    }

    @Test
    public void correctlyEvaluatesProgramWithVariables() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTopTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> VValue.value(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue(), ctx.env()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> VValue.value(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value(), ctx.env()));
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
        var resolved = program.resolveTypes(env, evaluationSpec).result();
        Assertions.assertTrue(resolved.isPresent());
        var result = Assertions.assertDoesNotThrow(() -> resolved.get().evaluate(EvaluationContext.builder(evaluationSpec).addVariable("var1", VValue.value(intType, 5, env)).addVariable("var2", VValue.value(intType, 10, env)).build(env)));
        Assertions.assertEquals(intType, result.type());
        Assertions.assertEquals(15, ((Number) result.value()).intValue());
    }

    @Test
    public void correctlyEvaluatesProgramWithObjectLiterals() {
        class MySpecialObject { // inline records when
            private final int a;
            private final int b;

            MySpecialObject(int a, int b) {
                this.a = a;
                this.b = b;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || this.getClass() != o.getClass()) return false;
                MySpecialObject that = (MySpecialObject) o;
                return this.a == that.a && this.b == that.b;
            }

            @Override
            public int hashCode() {
                return Objects.hash(this.a, this.b);
            }
        }
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var mySpecialType = VType.create(TypeToken.of(MySpecialObject.class), Map.of("a", intType, "b", intType), map -> new MySpecialObject(map.get("a").<Number>getUnchecked().intValue(), map.get("b").<Number>getUnchecked().intValue()));
        var anyType = VType.createTopTemplate();
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(Map.of("a", numType, "b", numType), numType), (ctx, sig, a) -> VValue.value(sig.outputType(), ((Number) a.get("a").value()).doubleValue() + ((Number) a.get("b").value()).doubleValue(), ctx.env()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(Map.of("predicate", boolType, "a", anyType, "b", anyType), anyType), (ctx, sig, a) -> VValue.value(sig.outputType(), (boolean) a.get("predicate").value() ? a.get("a").value() : a.get("b").value(), ctx.env()));
        var evaluationSpec = new EvaluationContext.Spec(Map.of("var1", intType, "var2", intType));
        var env = new VEnvironmentImpl();
        env.registerType("int", intType);
        env.registerType("double", doubleType);
        env.registerType("number", numType);
        env.registerType("bool", boolType);
        env.registerType("my_special_type", mySpecialType);
        env.registerType("any", anyType);
        env.registerFunction(addFunction);
        env.registerFunction(ifElseFunction);
        var program = VExpression.functionApplication(ifElseFunction, Map.of(
                "predicate", VExpression.value(boolType, true),
                "a", VExpression.object(
                        env.createTypeNamer().name(mySpecialType),
                        Map.of(
                                "a", VExpression.functionApplication(addFunction, Map.of(
                                        "a", VExpression.variable("var1"),
                                        "b", VExpression.variable("var2"))),
                                "b", VExpression.variable("var1"))),
                "b", VExpression.object(
                        env.createTypeNamer().name(mySpecialType),
                        Map.of(
                                "a", VExpression.variable("var2"),
                                "b", VExpression.variable("var1")))));
        var resolved = program.resolveTypes(env, evaluationSpec).result();
        Assertions.assertTrue(resolved.isPresent());
        var result = Assertions.assertDoesNotThrow(() -> resolved.get().evaluate(EvaluationContext.builder(evaluationSpec).addVariable("var1", VValue.value(intType, 5, env)).addVariable("var2", VValue.value(intType, 10, env)).build(env)));
        Assertions.assertEquals(mySpecialType, result.type());
        Assertions.assertEquals(new MySpecialObject(5 + 10, 5), result.value());
    }

    @Test
    public void correctlyEvaluatesProgramWithList() {
        var intType = VType.create();
        var doubleType = VType.create();
        var numType = VType.createTemplate(intType, doubleType);
        var boolType = VType.create();
        var anyType = VType.createTopTemplate();
        var bareListType = VType.create();
        var evaluationSpec = new EvaluationContext.Spec(Map.of("var1", intType));
        var env = new VEnvironmentImpl();
        env.registerType("int", intType);
        env.registerType("double", doubleType);
        env.registerType("number", numType);
        env.registerType("bool", boolType);
        env.registerType("any", anyType);
        env.registerType("list", bareListType);
        var program = VExpression.list(List.of(
                VExpression.value(intType, 3),
                VExpression.value(doubleType, 10.0),
                VExpression.variable("var1")));
        var resolved = program.resolveTypes(env, evaluationSpec).result();
        Assertions.assertTrue(resolved.isPresent());
        var result = Assertions.assertDoesNotThrow(() -> resolved.get().evaluate(EvaluationContext.builder(evaluationSpec).addVariable("var1", VValue.value(intType, 5, env)).build(env)));
        Assertions.assertEquals(env.listType().with(0, numType), result.type());
        Assertions.assertEquals(List.of(VValue.value(intType, 3, env), VValue.value(doubleType, 10.0, env), VValue.value(intType, 5, env)), result.value());
    }
}
