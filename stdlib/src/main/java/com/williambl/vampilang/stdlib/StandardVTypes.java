package com.williambl.vampilang.stdlib;

import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;

public final class StandardVTypes {
    public static final VType NUMBER = new VType();
    public static final VType STRING = new VType();
    public static final VType BOOLEAN = new VType();
    public static final VType TEMPLATE_ANY = new VTemplateType(null);
    public static final VType MATCH_CASE = new VType();
}
