package com.williambl.vampilang.stdlib;

import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public final class ArithmeticVFunctions {

    //binary operators
    public static final VFunctionDefinition ADD = fromBinaryOperator("add", Double::sum);
    public static final VFunctionDefinition SUBTRACT = fromBinaryOperator("subtract", (a, b) -> a - b);
    public static final VFunctionDefinition MULTIPLY = fromBinaryOperator("multiply", (a, b) -> a * b);
    public static final VFunctionDefinition DIVIDE = fromBinaryOperator("divide", (a, b) -> a / b);
    public static final VFunctionDefinition MODULO = fromBinaryOperator("modulo", (a, b) -> a % b);
    public static final VFunctionDefinition POWER = fromBinaryOperator("power", Math::pow);
    public static final VFunctionDefinition MAX = fromBinaryOperator("max", Math::max);
    public static final VFunctionDefinition MIN = fromBinaryOperator("min", Math::min);

    // unary operators
    public static final VFunctionDefinition ABSOLUTE = fromUnaryOperator("absolute", Math::abs);
    public static final VFunctionDefinition NEGATE = fromUnaryOperator("negate", a -> -a);
    public static final VFunctionDefinition SQUARE_ROOT = fromUnaryOperator("square_root", Math::sqrt);

    // trigonometry
    public static final VFunctionDefinition SINE = fromUnaryOperator("sine", Math::sin);
    public static final VFunctionDefinition COSINE = fromUnaryOperator("cosine", Math::cos);
    public static final VFunctionDefinition TANGENT = fromUnaryOperator("tangent", Math::tan);

    // polynomial
    public static final VFunctionDefinition POLYNOMIAL = new VFunctionDefinition("polynomial", new VFunctionSignature(
            Map.of("coefficients", StandardVTypes.LIST.with(0, StandardVTypes.NUMBER), "input", StandardVTypes.NUMBER),
            StandardVTypes.NUMBER),
            (ctx, sig, args) -> {
                @SuppressWarnings("unchecked")
                List<Double> coefficients = (List<Double>) args.get("coefficients").value();
                double input = (double) args.get("input").value();
                double result = 0;
                for (int i = 0; i < coefficients.size(); i++) {
                    result += coefficients.get(i) * Math.pow(input, i);
                }

                return new VValue(sig.outputType(), result);
            });

    public static VFunctionDefinition fromBinaryOperator(String name, DoubleBinaryOperator operator) {
        return new VFunctionDefinition(
                name,
                new VFunctionSignature(Map.of("a", StandardVTypes.NUMBER, "b", StandardVTypes.NUMBER), StandardVTypes.NUMBER),
                (ctx, sig, args) -> new VValue(sig.outputType(), operator.applyAsDouble((Double) args.get("a").value(), (Double) args.get("b").value())));
    }

    public static VFunctionDefinition fromUnaryOperator(String name, DoubleUnaryOperator operator) {
        return new VFunctionDefinition(
                name,
                new VFunctionSignature(Map.of("operand", StandardVTypes.NUMBER), StandardVTypes.NUMBER),
                (ctx, sig, args) -> new VValue(sig.outputType(), operator.applyAsDouble((Double) args.get("operand").value())));
    }
}
