package com.williambl.vampilang.stdlib.test;

import com.google.common.collect.Sets;
import com.williambl.vampilang.lang.*;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.stdlib.ArithmeticVFunctions;
import com.williambl.vampilang.stdlib.LogicVFunctions;
import com.williambl.vampilang.stdlib.StandardVFunctions;
import com.williambl.vampilang.stdlib.StandardVTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class StandardVFunctionsTest {
    @Test
    public void ifElseTest() {
        for (var value : List.of(true, false)) {
            var predicateExpr = VExpression.value(StandardVTypes.BOOLEAN, value);
            var aExpr = VExpression.value(StandardVTypes.STRING, "a");
            var bExpr = VExpression.value(StandardVTypes.STRING, "b");
            var expr = VExpression.functionApplication(StandardVFunctions.IF_ELSE, Map.of("predicate", predicateExpr, "a", aExpr, "b", bExpr)).resolveTypes(ENV, new EvaluationContext.Spec()).result();
            Assertions.assertTrue(expr.isPresent());
            var res = expr.get().evaluate(new EvaluationContext());
            var expected = value ? "a" : "b";
            Assertions.assertEquals(StandardVTypes.STRING, res.type());
            Assertions.assertEquals(expected, res.value());
        }
    }

    @Test
    public void matchTest() {
        var values = List.of(1., 2., 3., 4.);
        var cases = VExpression.value(StandardVTypes.LIST.with(0, StandardVTypes.MATCH_CASE), values.stream()
                .map(c -> Map.entry(c, Double.toString(c)))
                .toList());
        var deefault = VExpression.value(StandardVTypes.STRING, "?");
        var input = VExpression.variable("the_input");
        var spec = new EvaluationContext.Spec(Map.of("the_input", StandardVTypes.NUMBER));
        var expr = VExpression.functionApplication(StandardVFunctions.MATCH, Map.of(
                "input", input,
                "cases", cases,
                "default", deefault
        )).resolveTypes(ENV, spec).result();
        Assertions.assertTrue(expr.isPresent());
        for (var value : values) {
            var res = expr.get().evaluate(EvaluationContext.builder(spec).addVariable("the_input", new VValue(StandardVTypes.NUMBER, value)).build());
            var expected = Double.toString(value);
            Assertions.assertEquals(StandardVTypes.STRING, res.type());
            Assertions.assertEquals(expected, res.value());
        }
    }

    @Test
    public void equalsTest() {
        @SuppressWarnings("StringOperationCanBeSimplified") Set<Supplier<VValue>> values
                = Set.of(() -> new VValue(StandardVTypes.NUMBER, 3.32442), () -> new VValue(StandardVTypes.STRING, new String("yay :3")));
        var inputA = VExpression.variable("the_input_a");
        var inputB = VExpression.variable("the_input_b");
        var equalsExpr = VExpression.functionApplication(StandardVFunctions.EQUALS, Map.of(
                "a", inputA,
                "b", inputB
        ));
        var notEqualsExpr = VExpression.functionApplication(StandardVFunctions.NOT_EQUALS, Map.of(
                "a", inputA,
                "b", inputB
        ));
        for (var testCase : Sets.cartesianProduct(values, values)) {
            var a = testCase.get(0).get();
            var b = testCase.get(1).get();
            var spec = new EvaluationContext.Spec(Map.of("the_input_a", a.type(), "the_input_b", b.type()));
            var equalsRes = equalsExpr.resolveTypes(ENV, spec).map(e -> e.evaluate(EvaluationContext.builder(spec).addVariable("the_input_a", a).addVariable("the_input_b", b).build()));
            var notEqualsRes = notEqualsExpr.resolveTypes(ENV, spec).map(e -> e.evaluate(EvaluationContext.builder(spec).addVariable("the_input_a", a).addVariable("the_input_b", b).build()));
            var expected = Objects.equals(a, b);
            Assertions.assertEquals(StandardVTypes.BOOLEAN, equalsRes.result().map(VValue::type).orElse(null));
            Assertions.assertEquals(StandardVTypes.BOOLEAN, notEqualsRes.result().map(VValue::type).orElse(null));
            Assertions.assertEquals(expected, equalsRes.result().map(VValue::value).orElse(null));
            Assertions.assertEquals(!expected, notEqualsRes.result().map(VValue::value).orElse(null));
        }
    }

    //TODO comparison tests

    private static final VEnvironment ENV = new VEnvironmentImpl();
    static {
        StandardVTypes.register(ENV);
        StandardVFunctions.register(ENV);
    }
}
