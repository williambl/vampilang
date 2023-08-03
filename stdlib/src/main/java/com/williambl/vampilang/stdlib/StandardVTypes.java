package com.williambl.vampilang.stdlib;

import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public final class StandardVTypes {
    public static final VType NUMBER = new VType();
    public static final VType STRING = new VType();
    public static final VType BOOLEAN = new VType();
    public static final VType TEMPLATE_ANY = new VTemplateType(null);
    private static final VType RAW_LIST = new VType();
    public static final VParameterisedType LIST = new VParameterisedType(RAW_LIST, List.of(TEMPLATE_ANY));
    private static final VType RAW_MATCH_CASE = new VType();
    public static final VParameterisedType MATCH_CASE = new VParameterisedType(RAW_MATCH_CASE, List.of(BOOLEAN, TEMPLATE_ANY));
}
