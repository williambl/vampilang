package com.williambl.vampilang.stdlib;

import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;

import java.util.List;
import java.util.Map;

//TODO tests for all these
//TODO comparisons
//TODO 'create object' function
public class StandardVFunctions {
    public static final VFunctionDefinition IF_ELSE = new VFunctionDefinition("if-else",
            new VFunctionSignature(
                    Map.of("predicate", StandardVTypes.BOOLEAN, "a", StandardVTypes.TEMPLATE_ANY, "b", StandardVTypes.TEMPLATE_ANY),
                    StandardVTypes.TEMPLATE_ANY),
            (ctx, sig, args) -> (Boolean) args.get("predicate").value() ? args.get("a") : args.get("b"));

    public static final VFunctionDefinition MATCH = new VFunctionDefinition("match",
            new VFunctionSignature(
                    Map.of("cases", StandardVTypes.LIST.with(0, StandardVTypes.MATCH_CASE), "default", StandardVTypes.TEMPLATE_ANY),
                    StandardVTypes.TEMPLATE_ANY),
            (ctx, sig, args) -> {
                @SuppressWarnings("unchecked")
                List<Map.Entry<Boolean, Object>> cases = (List<Map.Entry<Boolean, Object>>) args.get("cases").value();
                Object defaultVal = args.get("default").value();
                return new VValue(sig.outputType(), cases.stream().filter(Map.Entry::getKey).map(Map.Entry::getValue).findFirst().orElse(defaultVal));
            });
}
