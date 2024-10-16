package com.williambl.vampilang.lang.test;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.VEnvironmentImpl;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypesTest {
    @Test
    public void typeContainsItself() {
        var type = VType.create();
        Assertions.assertTrue(type.contains(type, new VEnvironmentImpl()));
    }

    @Test
    public void moreGeneralTemplateTypeContainsTypeInBounds() {
        var type = VType.create();
        var template = VType.createTemplate(type);
        Assertions.assertTrue(template.contains(type, new VEnvironmentImpl()));
        Assertions.assertFalse(type.contains(template, new VEnvironmentImpl()));
    }

    @Test
    public void dynamicTemplateTypeContainsTypeInBounds() {
        var type = VType.create();
        var template = VType.createDynamicTemplate(t -> t == type);
        var env = new VEnvironmentImpl();
        env.registerType("type", type);
        env.registerType("template", template);
        Assertions.assertTrue(template.contains(type, env));
        Assertions.assertFalse(type.contains(template, env));
    }

    @Test
    public void moreGeneralParameterisedTypeContainsSpecificParameterisedType() {
        var type = VType.create();
        var template = VType.createTemplate(type);
        var listBareType = VType.create();
        var generalParamedListType = VType.createParameterised(listBareType, template);
        var specificParamedListType = generalParamedListType.with(0, type);
        Assertions.assertTrue(generalParamedListType.contains(specificParamedListType, new VEnvironmentImpl()));
        Assertions.assertFalse(specificParamedListType.contains(generalParamedListType, new VEnvironmentImpl()));
    }

    @Test
    public void typedVTypeAcceptsObjects() {
        var type = VType.create(TypeToken.of(Double.class));
        Assertions.assertTrue(type.accepts(5.0, new VEnvironmentImpl()));
        Assertions.assertFalse(type.accepts("hi", new VEnvironmentImpl()));
    }
}
