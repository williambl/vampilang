package com.williambl.vampilang.stdlib;

import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;

import java.util.Map;

public class StandardVFunctions {
    public static final VFunctionDefinition IF_ELSE = new VFunctionDefinition("if-else",
            new VFunctionSignature(
                    Map.of("predicate", StandardVTypes.BOOLEAN, "a", StandardVTypes.TEMPLATE_ANY, "b", StandardVTypes.TEMPLATE_ANY),
                    StandardVTypes.TEMPLATE_ANY),
            (ctx, sig, args) -> (Boolean) args.get("predicate").value() ? args.get("a") : args.get("b"));

    public static final VFunctionDefinition MATCH = new VFunctionDefinition("match",
            new VFunctionSignature(
                    Map.of("cases", StandardVTypes.MATCH_CASE, "default", StandardVTypes.TEMPLATE_ANY),
                    StandardVTypes.TEMPLATE_ANY),
            (ctx, sig, args) -> )
}
