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
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class StandardVFunctionsTest {
    @Test
    public void ifElseTest() {
        for (var value : List.of(true, false)) {
            var predicateExpr = VExpression.value(StandardVTypes.BOOLEAN, value);
            var aExpr = VExpression.value(StandardVTypes.STRING, "a");
            var bExpr = VExpression.value(StandardVTypes.STRING, "b");
            var expr = VExpression.functionApplication(StandardVFunctions.IF_ELSE, Map.of("predicate", predicateExpr, "a", aExpr, "b", bExpr)).resolveTypes(ENV, new EvaluationContext.Spec());
            var res = expr.evaluate(new EvaluationContext());
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
        )).resolveTypes(ENV, spec);
        for (var value : values) {
            var res = expr.evaluate(EvaluationContext.builder(spec).addVariable("the_input", new VValue(StandardVTypes.NUMBER, value)).build());
            var expected = Double.toString(value);
            Assertions.assertEquals(StandardVTypes.STRING, res.type());
            Assertions.assertEquals(expected, res.value());
        }
    }

    private static final VEnvironment ENV = new VEnvironmentImpl();
    static {
        StandardVTypes.register(ENV);
        StandardVFunctions.register(ENV);
    }

}
