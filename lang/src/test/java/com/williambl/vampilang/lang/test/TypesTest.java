package com.williambl.vampilang.lang.test;

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
        var type = new VType();
        Assertions.assertTrue(type.contains(type));
    }

    @Test
    public void moreGeneralTemplateTypeContainsTypeInBounds() {
        var type = new VType();
        var template = new VTemplateType(Set.of(type));
        Assertions.assertTrue(template.contains(type));
        Assertions.assertFalse(type.contains(template));
    }

    @Test
    public void moreGeneralParameterisedTypeContainsSpecificParameterisedType() {
        var type = new VType();
        var template = new VTemplateType(Set.of(type));
        var listBareType = new VType();
        var generalParamedListType = new VParameterisedType(listBareType, List.of(template));
        var specificParamedListType = generalParamedListType.with(0, type);
        Assertions.assertTrue(generalParamedListType.contains(specificParamedListType));
        Assertions.assertFalse(specificParamedListType.contains(generalParamedListType));
    }
}
