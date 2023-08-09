package com.williambl.vampilang.stdlib.test;

import com.google.common.collect.Sets;
import com.williambl.vampilang.lang.*;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.stdlib.ArithmeticVFunctions;
import com.williambl.vampilang.stdlib.LogicVFunctions;
import com.williambl.vampilang.stdlib.StandardVTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Stream;

public class LogicVFunctionsTest {
    @Test
    public void andTest() {
        fromMultiOperator(LogicVFunctions.AND, MultiOperatorTestCase.create(bools -> bools.allMatch(Boolean::booleanValue)));
    }

    @Test
    public void orTest() {
        fromMultiOperator(LogicVFunctions.OR, MultiOperatorTestCase.create(bools -> bools.anyMatch(Boolean::booleanValue)));
    }

    @Test
    public void notTest() {
        fromUnaryOperator(LogicVFunctions.NOT, UnaryOperatorTestCase.create(b -> !b));
    }

    private static final Set<Boolean> INPUTS = Set.of(true, false);
    private static final Set<List<Boolean>> MULTI_INPUTS = Set.of(
            List.of(true),
            List.of(false),
            List.of(true, false),
            List.of(false, false, true),
            List.of(true, true, true, false, false, true, false),
            List.of(true, false, false, true, false, true, false));

    private static final VEnvironment ENV = new VEnvironmentImpl();
    static {
        StandardVTypes.register(ENV);
        LogicVFunctions.register(ENV);
    }

    private static void fromBinaryOperator(VFunctionDefinition function, List<BinaryOperatorTestCase> cases) {
        for (var test : cases) {
            var expr = VExpression.functionApplication(function, Map.of("a", VExpression.value(StandardVTypes.BOOLEAN, test.a()), "b", VExpression.value(StandardVTypes.BOOLEAN, test.b()))).resolveTypes(ENV, new EvaluationContext.Spec()).result();
            Assertions.assertTrue(expr.isPresent());
            var res = expr.get().evaluate(new EvaluationContext());
            Assertions.assertEquals(StandardVTypes.BOOLEAN, res.type());
            Assertions.assertEquals(test.res(), res.value());
        }
    }

    private static void fromMultiOperator(VFunctionDefinition function, List<MultiOperatorTestCase> cases) {
        for (var test : cases) {
            var expr = VExpression.functionApplication(function, Map.of("operands", VExpression.list(test.inputs().stream().map(b -> VExpression.value(StandardVTypes.BOOLEAN, b)).toList()))).resolveTypes(ENV, new EvaluationContext.Spec()).result();
            Assertions.assertTrue(expr.isPresent());
            var res = expr.get().evaluate(new EvaluationContext());
            Assertions.assertEquals(StandardVTypes.BOOLEAN, res.type());
            Assertions.assertEquals(test.res(), res.value());
        }
    }

    private static void fromUnaryOperator(VFunctionDefinition function, List<UnaryOperatorTestCase> cases) {
        for (var test : cases) {
            var expr = VExpression.functionApplication(function, Map.of("operand", VExpression.value(StandardVTypes.BOOLEAN, test.operand()))).resolveTypes(ENV, new EvaluationContext.Spec()).result();
            Assertions.assertTrue(expr.isPresent());
            var res = expr.get().evaluate(new EvaluationContext());
            Assertions.assertEquals(StandardVTypes.BOOLEAN, res.type());
            Assertions.assertEquals(test.res(), res.value());
        }
    }

    private record BinaryOperatorTestCase(boolean a, boolean b, boolean res) {
        static List<BinaryOperatorTestCase> create(BinaryOperator<Boolean> operator) {
            var product = Sets.cartesianProduct(INPUTS, INPUTS);
            return product.stream().map(ds -> new BinaryOperatorTestCase(ds.get(0), ds.get(1), operator.apply(ds.get(0), ds.get(1)))).toList();
        }
    }

    private record MultiOperatorTestCase(List<Boolean> inputs, boolean res) {
        static List<MultiOperatorTestCase> create(Function<Stream<Boolean>, Boolean> operator) {
            return MULTI_INPUTS.stream().map(ds -> new MultiOperatorTestCase(ds, operator.apply(ds.stream()))).toList();
        }
    }

    private record UnaryOperatorTestCase(boolean operand, boolean res) {
        static List<UnaryOperatorTestCase> create(UnaryOperator<Boolean> operator) {
            return INPUTS.stream().map(d -> new UnaryOperatorTestCase(d, operator.apply(d))).toList();
        }
    }
}
