package com.williambl.vampilang.stdlib.test;

import com.google.common.collect.Sets;
import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VEnvironmentImpl;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.stdlib.ArithmeticVFunctions;
import com.williambl.vampilang.stdlib.StandardVTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class ArithmeticVFunctionsTest {
    @Test
    public void addTest() {
        fromBinaryOperator(ArithmeticVFunctions.ADD, BinaryOperatorTestCase.create(Double::sum));
    }

    @Test
    public void subtractTest() {
        fromBinaryOperator(ArithmeticVFunctions.SUBTRACT, BinaryOperatorTestCase.create((a, b) -> a - b));
    }

    @Test
    public void multiplyTest() {
        fromBinaryOperator(ArithmeticVFunctions.MULTIPLY, BinaryOperatorTestCase.create((a, b) -> a * b));
    }

    @Test
    public void divideTest() {
        fromBinaryOperator(ArithmeticVFunctions.DIVIDE, BinaryOperatorTestCase.create((a, b) -> a / b));
    }

    @Test
    public void moduloTest() {
        fromBinaryOperator(ArithmeticVFunctions.MODULO, BinaryOperatorTestCase.create((a, b) -> a % b));
    }

    @Test
    public void powerTest() {
        fromBinaryOperator(ArithmeticVFunctions.POWER, BinaryOperatorTestCase.create(Math::pow));
    }

    @Test
    public void maxTest() {
        fromBinaryOperator(ArithmeticVFunctions.MAX, BinaryOperatorTestCase.create(Math::max));
    }

    @Test
    public void minTest() {
        fromBinaryOperator(ArithmeticVFunctions.MIN, BinaryOperatorTestCase.create(Math::min));
    }

    @Test
    public void absoluteTest() {
        fromUnaryOperator(ArithmeticVFunctions.ABSOLUTE, UnaryOperatorTestCase.create(Math::abs));
    }

    @Test
    public void negateTest() {
        fromUnaryOperator(ArithmeticVFunctions.NEGATE, UnaryOperatorTestCase.create(a -> -a));
    }

    @Test
    public void square_rootTest() {
        fromUnaryOperator(ArithmeticVFunctions.SQUARE_ROOT, UnaryOperatorTestCase.create(Math::sqrt));
    }

    @Test
    public void sineTest() {
        fromUnaryOperator(ArithmeticVFunctions.SINE, UnaryOperatorTestCase.create(Math::sin));
    }

    @Test
    public void cosineTest() {
        fromUnaryOperator(ArithmeticVFunctions.COSINE, UnaryOperatorTestCase.create(Math::cos));
    }

    @Test
    public void tangentTest() {
        fromUnaryOperator(ArithmeticVFunctions.TANGENT, UnaryOperatorTestCase.create(Math::tan));
    }

    @Test
    public void polynomialTest() {
        for (var coefficients : List.of(List.of(0.), List.of(1.), List.of(-5., 5., 2.), List.of(10., 30., 200.))) {
            var coefficientsExpr = VExpression.list(coefficients.stream().map(c -> VExpression.value(StandardVTypes.NUMBER, c)).toList());
            for (var test : INPUTS) {
                var expr = VExpression.functionApplication(ArithmeticVFunctions.POLYNOMIAL, Map.of("coefficients", coefficientsExpr, "input", VExpression.value(StandardVTypes.NUMBER, test))).resolveTypes(ENV, new EvaluationContext.Spec()).result();
                Assertions.assertTrue(expr.isPresent());
                var res = expr.get().evaluate(new EvaluationContext(ENV));
                double result = 0;
                for (int i = 0; i < coefficients.size(); i++) {
                    result += coefficients.get(i) * Math.pow(test, i);
                }
                Assertions.assertEquals(StandardVTypes.NUMBER, res.type());
                Assertions.assertEquals(result, res.value());
            }
        }
    }

    private static final Set<Double> INPUTS = Set.of(-10., -200., 10., 5., 100., 200., 4.5, Double.NEGATIVE_INFINITY, (double) Float.MIN_VALUE);
    private static final VEnvironment ENV = new VEnvironmentImpl();
    static {
        StandardVTypes.register(ENV);
        ArithmeticVFunctions.register(ENV);
    }

    private static void fromBinaryOperator(VFunctionDefinition function, List<BinaryOperatorTestCase> cases) {
        for (var test : cases) {
            var expr = VExpression.functionApplication(function, Map.of("a", VExpression.value(StandardVTypes.NUMBER, test.a()), "b", VExpression.value(StandardVTypes.NUMBER, test.b()))).resolveTypes(ENV, new EvaluationContext.Spec()).result();
            Assertions.assertTrue(expr.isPresent());
            var res = expr.get().evaluate(new EvaluationContext(ENV));
            Assertions.assertEquals(StandardVTypes.NUMBER, res.type());
            Assertions.assertEquals(test.res(), res.value());
        }
    }

    private static void fromUnaryOperator(VFunctionDefinition function, List<UnaryOperatorTestCase> cases) {
        for (var test : cases) {
            var expr = VExpression.functionApplication(function, Map.of("operand", VExpression.value(StandardVTypes.NUMBER, test.operand()))).resolveTypes(ENV, new EvaluationContext.Spec()).result();
            Assertions.assertTrue(expr.isPresent());
            var res = expr.get().evaluate(new EvaluationContext(ENV));
            Assertions.assertEquals(StandardVTypes.NUMBER, res.type());
            Assertions.assertEquals(test.res(), res.value());
        }
    }

    private record BinaryOperatorTestCase(double a, double b, double res) {
        static List<BinaryOperatorTestCase> create(DoubleBinaryOperator operator) {
            var product = Sets.cartesianProduct(INPUTS, INPUTS);
            return product.stream().map(ds -> new BinaryOperatorTestCase(ds.get(0), ds.get(1), operator.applyAsDouble(ds.get(0), ds.get(1)))).toList();
        }
    }

    private record UnaryOperatorTestCase(double operand, double res) {
        static List<UnaryOperatorTestCase> create(DoubleUnaryOperator operator) {
            return INPUTS.stream().map(d -> new UnaryOperatorTestCase(d, operator.applyAsDouble(d))).toList();
        }
    }
}
