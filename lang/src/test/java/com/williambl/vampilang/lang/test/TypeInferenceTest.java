package com.williambl.vampilang.lang.test;

import com.williambl.vampilang.lang.EvaluationContext;
import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeInferenceTest {
    @Test
    public void correctlyInfersSingleTypeVariable() {
        var templateType = new VTemplateType(null);
        var identityFunctionSignature = new VFunctionSignature(Map.of("x", templateType), templateType);
        var concreteType = new VType();
        var resolvedFunctionSignature = identityFunctionSignature.resolveTypes(Map.of("x", concreteType));
        Assertions.assertFalse(resolvedFunctionSignature.inputTypes().get("x") instanceof VTemplateType);
        Assertions.assertFalse(resolvedFunctionSignature.outputType() instanceof VTemplateType);
        Assertions.assertEquals(concreteType, resolvedFunctionSignature.inputTypes().get("x"));
        Assertions.assertEquals(concreteType, resolvedFunctionSignature.outputType());
    }

    @Test
    public void correctlyInfersTypeVariableWithTwoMatchingInputTypes() {
        var templateType = new VTemplateType(null);
        var functionSignature = new VFunctionSignature(Map.of("a", templateType, "b", templateType), templateType);
        var concreteTypeA = new VType();
        var concreteTypeB = new VType();
        var aOrBType = new VTemplateType(Set.of(concreteTypeA, concreteTypeB));
        var resolvedFunctionSignature = Assertions.assertDoesNotThrow(() -> functionSignature.resolveTypes(Map.of("a", concreteTypeA, "b", aOrBType)));
        Assertions.assertEquals(aOrBType, resolvedFunctionSignature.inputTypes().get("a"));
        Assertions.assertEquals(aOrBType, resolvedFunctionSignature.inputTypes().get("b"));
        Assertions.assertEquals(aOrBType, resolvedFunctionSignature.outputType());
    }

    @Test
    public void failsToInferNonMatchingInputTypes() {
        var templateType = new VTemplateType(null);
        var functionSignature = new VFunctionSignature(Map.of("a", templateType, "b", templateType), templateType);
        var concreteTypeA = new VType();
        var concreteTypeB = new VType();
        Assertions.assertThrows(IllegalStateException.class, () -> functionSignature.resolveTypes(Map.of("a", concreteTypeA, "b", concreteTypeB)));
    }

    @Test
    public void correctlyInfersParameterisedType() {
        var ctx = new EvaluationContext();
        var bareListType = new VType();     // List
        ctx.addName(bareListType, "List");
        var templateType = new VTemplateType(null); // Any
        ctx.addName(templateType, "Any");
        var listType = new VParameterisedType(bareListType, List.of(templateType)); // List<? extends Any>
        var functionSignature = new VFunctionSignature(Map.of("a", listType), templateType); // <T extends Any> (List<T>) -> T
        var aType = new VType();    // A
        ctx.addName(aType, "A");
        var aListType = listType.with(0, aType);    // List<A>
        var resolvedFunctionSignature = Assertions.assertDoesNotThrow(() -> functionSignature.resolveTypes(Map.of("a", aListType)));
        Assertions.assertEquals(aListType, resolvedFunctionSignature.inputTypes().get("a"));
        Assertions.assertEquals(aType, resolvedFunctionSignature.outputType());
    }
}
