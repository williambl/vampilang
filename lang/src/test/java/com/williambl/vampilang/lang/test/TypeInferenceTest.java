package com.williambl.vampilang.lang.test;

import com.williambl.vampilang.lang.TypeNamer;
import com.williambl.vampilang.lang.function.VFunctionSignature;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TypeInferenceTest {
    @Test
    public void correctlyInfersSingleTypeVariable() {
        var templateType = VType.createTemplate();
        var identityFunctionSignature = new VFunctionSignature(Map.of("x", templateType), templateType);
        var concreteType = VType.create();
        var resolvedFunctionSignature = identityFunctionSignature.resolveTypes(Map.of("x", concreteType)).result();
        Assertions.assertTrue(resolvedFunctionSignature.isPresent());
        Assertions.assertFalse(resolvedFunctionSignature.get().inputTypes().get("x") instanceof VTemplateType);
        Assertions.assertFalse(resolvedFunctionSignature.get().outputType() instanceof VTemplateType);
        Assertions.assertEquals(concreteType, resolvedFunctionSignature.get().inputTypes().get("x"));
        Assertions.assertEquals(concreteType, resolvedFunctionSignature.get().outputType());
    }

    @Test
    public void correctlyInfersTypeVariableWithTwoMatchingInputTypes() {
        var templateType = VType.createTemplate();
        var functionSignature = new VFunctionSignature(Map.of("a", templateType, "b", templateType), templateType);
        var concreteTypeA = VType.create();
        var concreteTypeB = VType.create();
        var aOrBType = VType.createTemplate(concreteTypeA, concreteTypeB);
        var resolvedFunctionSignature = functionSignature.resolveTypes(Map.of("a", concreteTypeA, "b", aOrBType)).result();
        Assertions.assertTrue(resolvedFunctionSignature.isPresent());
        Assertions.assertEquals(aOrBType, resolvedFunctionSignature.get().inputTypes().get("a"));
        Assertions.assertEquals(aOrBType, resolvedFunctionSignature.get().inputTypes().get("b"));
        Assertions.assertEquals(aOrBType, resolvedFunctionSignature.get().outputType());
    }

    @Test
    public void failsToInferNonMatchingInputTypes() {
        var templateType = VType.createTemplate();
        var functionSignature = new VFunctionSignature(Map.of("a", templateType, "b", templateType), templateType);
        var concreteTypeA = VType.create();
        var concreteTypeB = VType.create();
        Assertions.assertFalse(functionSignature.resolveTypes(Map.of("a", concreteTypeA, "b", concreteTypeB)).result().isPresent());
    }

    @Test
    public void correctlyInfersParameterisedType() {
        var ctx = new TypeNamer();
        var bareListType = VType.create();     // List
        ctx.addName(bareListType, "List");
        var templateType = VType.createTemplate(); // Any
        ctx.addName(templateType, "Any");
        var listType = VType.createParameterised(bareListType, templateType); // List<? extends Any>
        var functionSignature = new VFunctionSignature(Map.of("a", listType), templateType); // <T extends Any> (List<T>) -> T
        var aType = VType.create();    // A
        ctx.addName(aType, "A");
        var aListType = listType.with(0, aType);    // List<A>
        var resolvedFunctionSignature = functionSignature.resolveTypes(Map.of("a", aListType)).result();
        Assertions.assertTrue(resolvedFunctionSignature.isPresent());
        Assertions.assertEquals(aListType, resolvedFunctionSignature.get().inputTypes().get("a"));
        Assertions.assertEquals(aType, resolvedFunctionSignature.get().outputType());
    }
}
