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
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(List.of(numType, numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get(0).value()).doubleValue() + ((Number) a.get(1).value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(List.of(boolType, anyType, anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get(0).value() ? a.get(1).value() : a.get(2).value()));
        var program = VExpression.functionApplication(ifElseFunction,
                VExpression.value(boolType, true),
                VExpression.functionApplication(addFunction,
                        VExpression.value(intType, 5),
                        VExpression.value(intType, 10)),
                VExpression.value(intType, 25)
        );
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
        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(List.of(numType, numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get(0).value()).doubleValue() + ((Number) a.get(1).value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(List.of(boolType, anyType, anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get(0).value() ? a.get(1).value() : a.get(2).value()));
        var program = VExpression.functionApplication(ifElseFunction,
                VExpression.functionApplication(ifElseFunction,
                        VExpression.value(boolType, true),
                        VExpression.value(boolType, true),
                        VExpression.value(boolType, false)),
                VExpression.functionApplication(addFunction,
                        VExpression.value(intType, 5),
                        VExpression.value(intType, 10)),
                VExpression.value(intType, 25)
        );
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

        var addFunction = new VFunctionDefinition("add", new VFunctionSignature(List.of(numType, numType), numType), (ctx, sig, a) -> new VValue(sig.outputType(), ((Number) a.get(0).value()).doubleValue() + ((Number) a.get(1).value()).doubleValue()));
        var ifElseFunction = new VFunctionDefinition("if-else", new VFunctionSignature(List.of(boolType, anyType, anyType), anyType), (ctx, sig, a) -> new VValue(sig.outputType(), (boolean) a.get(0).value() ? a.get(1).value() : a.get(2).value()));
        var program = VExpression.functionApplication(ifElseFunction,
                VExpression.functionApplication(ifElseFunction,
                        VExpression.value(boolType, true),
                        VExpression.value(boolType, true),
                        VExpression.value(boolType, false)),
                VExpression.functionApplication(addFunction,
                        VExpression.value(intType, 5),
                        VExpression.value(intType, 10)),
                VExpression.value(intType, 25)
        );
        var resolved = Assertions.assertDoesNotThrow(program::resolveTypes);
        Assertions.assertEquals("(function if-else (function if-else (value true : bool) (value true : bool) (value false : bool) : bool, type1, type1 -> type1) (function add (value 5 : int) (value 10 : int) : type2[double|int], type2[double|int] -> type2[double|int]) (value 25 : int) : bool, type3, type3 -> type3)", program.toString(new EvaluationContext(evaluationCtx)));
        Assertions.assertEquals("(function if-else (function if-else (value true : bool) (value true : bool) (value false : bool) : bool, bool, bool -> bool) (function add (value 5 : int) (value 10 : int) : int, int -> int) (value 25 : int) : bool, int, int -> int)", resolved.toString(new EvaluationContext(evaluationCtx)));
    }
}
