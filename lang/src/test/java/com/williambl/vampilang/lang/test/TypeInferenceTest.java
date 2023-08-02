package com.williambl.vampilang.lang.test;

import com.williambl.vampilang.lang.VValue;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.function.VFunctionSignature;
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
        Assertions.assertFalse(resolvedFunctionSignature.inputTypes().get("x").isTemplate());
        Assertions.assertFalse(resolvedFunctionSignature.outputType().isTemplate());
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
}
