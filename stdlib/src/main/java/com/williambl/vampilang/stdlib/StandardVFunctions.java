package com.williambl.vampilang.stdlib;

import com.sun.source.tree.BreakTree;
import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class StandardVFunctions {
    public static final VFunctionDefinition IF_ELSE = new VFunctionDefinition("if-else",
            new VFunctionSignature(
                    Map.of("predicate", StandardVTypes.BOOLEAN, "a", StandardVTypes.TEMPLATE_ANY, "b", StandardVTypes.TEMPLATE_ANY),
                    StandardVTypes.TEMPLATE_ANY),
            (ctx, sig, args) -> (Boolean) args.get("predicate").value() ? args.get("a") : args.get("b"));

    private static final VType MATCH_ON_TYPE = StandardVTypes.TEMPLATE_ANY.uniquise(new HashMap<>());
    private static final VType MATCH_RESULT_TYPE = StandardVTypes.TEMPLATE_ANY.uniquise(new HashMap<>());
    public static final VFunctionDefinition MATCH = new VFunctionDefinition("match",
            new VFunctionSignature(
                    Map.of("input", MATCH_ON_TYPE, "cases", StandardVTypes.LIST.with(0, StandardVTypes.MATCH_CASE.with(0, MATCH_ON_TYPE).with(1, MATCH_RESULT_TYPE)), "default", MATCH_RESULT_TYPE),
                    MATCH_RESULT_TYPE),
            (ctx, sig, args) -> {
                Object input = args.get("input").value();
                List<Map.Entry<Object, Object>> cases = args.get("cases").getUnchecked();
                Object defaultVal = args.get("default").value();
                return new VValue(sig.outputType(), cases.stream().filter(kase -> Objects.equals(kase.getKey(), input)).map(Map.Entry::getValue).findFirst().orElse(defaultVal));
            });

    public static final VFunctionDefinition EQUALS = createComparison("==", Objects::equals);
    public static final VFunctionDefinition NOT_EQUALS = createComparison("!=", (a, b) -> !(Objects.equals(a, b)));
    public static final VFunctionDefinition LESS_THAN = createNumberComparison("<", (a, b) -> a < b);
    public static final VFunctionDefinition GREATER_THAN = createNumberComparison(">", (a, b) -> a > b);
    public static final VFunctionDefinition LESS_THAN_OR_EQUAL = createNumberComparison("<=", (a, b) -> a <= b);
    public static final VFunctionDefinition GREATER_THAN_OR_EQUAL = createNumberComparison(">=", (a, b) -> a >= b);

    public static final VFunctionDefinition MAP_OPTIONAL = create(() -> {
        var type = StandardVTypes.TEMPLATE_ANY.uniquise(new HashMap<>());
        var output = StandardVTypes.TEMPLATE_ANY.uniquise(new HashMap<>());
        return new VFunctionDefinition("map_optional",
                new VFunctionSignature(Map.of(
                        "optional", StandardVTypes.OPTIONAL.with(0, type),
                        "mapping", StandardVTypes.OPTIONAL_MAPPING.with(0, output).with(1, type)
                ), StandardVTypes.OPTIONAL.with(1, output)),
                (ctx, sig, args) -> {
                    var optContainingType = ((VParameterisedType) sig.inputTypes().get("optional")).parameters.get(0);
                    Optional<Object> opt = args.get("optional").getUnchecked();
                    VExpression mapping = args.get("mapping").getUnchecked();
                    Optional<Object> res = opt.map(o -> mapping.evaluate(ctx.with("unwrapped_optional", new VValue(optContainingType, o))).value());
                    return new VValue(sig.outputType(), res);
                });
    });

    public static final VFunctionDefinition UNWRAP_OPTIONAL = create(() -> {
        var type = StandardVTypes.TEMPLATE_ANY.uniquise(new HashMap<>());
        return new VFunctionDefinition("map_optional",
                new VFunctionSignature(Map.of(
                        "optional", StandardVTypes.OPTIONAL.with(0, type),
                        "fallback", type
                ), type),
                (ctx, sig, args) -> {
                    Optional<Object> opt = args.get("optional").getUnchecked();
                    return opt.map(o -> new VValue(sig.outputType(), o)).orElseGet(() -> args.get("fallback"));
                });
    });

    //TODO flatmap + filter


    private static VFunctionDefinition createNumberComparison(String name, BiPredicate<Double, Double> predicate) {
        return new VFunctionDefinition(name,
                new VFunctionSignature(
                        Map.of("a", StandardVTypes.NUMBER, "b", StandardVTypes.NUMBER),
                        StandardVTypes.BOOLEAN),
                (ctx, sig, args) -> new VValue(sig.outputType(), predicate.test(args.get("a").getUnchecked(), args.get("b").getUnchecked())));
    }

    private static VFunctionDefinition createComparison(String name, BiPredicate<Object, Object> predicate) {
        return new VFunctionDefinition(name,
                new VFunctionSignature(
                        Map.of("a", StandardVTypes.TEMPLATE_ANY.uniquise(new HashMap<>()), "b", StandardVTypes.TEMPLATE_ANY),
                        StandardVTypes.BOOLEAN),
                (ctx, sig, args) -> new VValue(sig.outputType(), predicate.test(args.get("a").getUnchecked(), args.get("b").getUnchecked())));
    }

    private static VFunctionDefinition create(Supplier<VFunctionDefinition> sup) {
        return sup.get();
    }

    public static void register(VEnvironment env) {
        env.registerFunction(IF_ELSE);
        env.registerFunction(MATCH);
        env.registerFunction(EQUALS);
        env.registerFunction(NOT_EQUALS);
        env.registerFunction(LESS_THAN);
        env.registerFunction(GREATER_THAN);
        env.registerFunction(LESS_THAN_OR_EQUAL);
        env.registerFunction(GREATER_THAN_OR_EQUAL);
        env.registerFunction(MAP_OPTIONAL);
        env.registerFunction(UNWRAP_OPTIONAL);
    }
}
