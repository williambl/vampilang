package com.williambl.vampilang.lang.test;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class TypesTest {
    @Test
    public void typeContainsItself() {
        var type = VType.create();
        Assertions.assertTrue(type.contains(type));
    }

    @Test
    public void moreGeneralTemplateTypeContainsTypeInBounds() {
        var type = VType.create();
        var template = VType.createTemplate(type);
        Assertions.assertTrue(template.contains(type));
        Assertions.assertFalse(type.contains(template));
    }

    @Test
    public void moreGeneralParameterisedTypeContainsSpecificParameterisedType() {
        var type = VType.create();
        var template = VType.createTemplate(type);
        var listBareType = VType.create();
        var generalParamedListType = VType.createParameterised(listBareType, template);
        var specificParamedListType = generalParamedListType.with(0, type);
        Assertions.assertTrue(generalParamedListType.contains(specificParamedListType));
        Assertions.assertFalse(specificParamedListType.contains(generalParamedListType));
    }

    @Test
    public void typedVTypeAcceptsObjects() {
        var type = VType.create(TypeToken.of(Double.class));
        Assertions.assertTrue(type.accepts(5.0));
        Assertions.assertFalse(type.accepts("hi"));
    }
}
