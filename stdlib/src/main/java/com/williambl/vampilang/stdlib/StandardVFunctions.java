package com.williambl.vampilang.stdlib;

import com.williambl.vampilang.lang.VEnvironment;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//TODO tests for all these
//TODO comparisons
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
                    StandardVTypes.TEMPLATE_ANY),
            (ctx, sig, args) -> {
                Object input = args.get("input").value();
                List<Map.Entry<Object, Object>> cases = args.get("cases").getUnchecked();
                Object defaultVal = args.get("default").value();
                return new VValue(sig.outputType(), cases.stream().filter(kase -> Objects.equals(kase.getKey(), input)).map(Map.Entry::getValue).findFirst().orElse(defaultVal));
            });

    public static void register(VEnvironment env) {
        env.registerFunction(IF_ELSE);
        env.registerFunction(MATCH);
    }
}
