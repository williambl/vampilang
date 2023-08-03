package com.williambl.vampilang.stdlib;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.type.*;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public final class StandardVTypes {
    public static final TypedVType<Double> NUMBER = VType.create(TypeToken.of(Double.class));
    public static final TypedVType<String> STRING = VType.create(TypeToken.of(String.class));
    public static final TypedVType<Boolean> BOOLEAN = VType.create(TypeToken.of(Boolean.class));
    public static final VTemplateType TEMPLATE_ANY = VType.createTemplate();
    private static final VType RAW_LIST = VType.create();
    public static final VParameterisedType LIST = VType.createParameterised(RAW_LIST, TEMPLATE_ANY);
    private static final VType RAW_MATCH_CASE = VType.create();
    public static final VParameterisedType MATCH_CASE = VType.createParameterised(RAW_MATCH_CASE, BOOLEAN, TEMPLATE_ANY);
}
