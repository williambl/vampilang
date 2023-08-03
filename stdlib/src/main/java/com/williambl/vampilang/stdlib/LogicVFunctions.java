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
import java.util.stream.Stream;

public final class LogicVFunctions {
    public static final VFunctionDefinition AND = fromMultiOperator("and", bools -> bools.allMatch(Boolean::booleanValue));
    public static final VFunctionDefinition OR = fromMultiOperator("or", bools -> bools.anyMatch(Boolean::booleanValue));
    public static final VFunctionDefinition NOT = fromUnaryOperator("not", bool -> !bool);

    public static VFunctionDefinition fromBinaryOperator(String name, BiFunction<Boolean, Boolean, Boolean> operator) {
        return new VFunctionDefinition(
                name,
                new VFunctionSignature(Map.of("a", StandardVTypes.BOOLEAN, "b", StandardVTypes.BOOLEAN), StandardVTypes.BOOLEAN),
                (ctx, sig, args) -> new VValue(sig.outputType(), operator.apply(args.get("a").get(StandardVTypes.BOOLEAN), args.get("b").get(StandardVTypes.BOOLEAN))));
    }

    @SuppressWarnings("unchecked")
    public static VFunctionDefinition fromMultiOperator(String name, Function<Stream<Boolean>, Boolean> operator) {
        return new VFunctionDefinition(
                name,
                new VFunctionSignature(Map.of("operands", StandardVTypes.LIST.with(0, StandardVTypes.BOOLEAN)), StandardVTypes.BOOLEAN),
                (ctx, sig, args) -> new VValue(sig.outputType(), operator.apply(((List<Boolean>) args.get("operands").value()).stream())));
    }

    public static VFunctionDefinition fromUnaryOperator(String name, Function<Boolean, Boolean> operator) {
        return new VFunctionDefinition(
                name,
                new VFunctionSignature(Map.of("operand", StandardVTypes.BOOLEAN), StandardVTypes.BOOLEAN),
                (ctx, sig, args) -> new VValue(sig.outputType(), operator.apply(args.get("operand").get(StandardVTypes.BOOLEAN))));
    }
}
