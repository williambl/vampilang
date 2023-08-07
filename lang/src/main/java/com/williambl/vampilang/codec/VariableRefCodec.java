package com.williambl.vampilang.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.VType;

public class VariableRefCodec {
    public static final Codec<VExpression.VariableRef> CODEC = Codec.STRING.fieldOf("var").codec().xmap(s -> (VExpression.VariableRef) VExpression.variable(s), VExpression.VariableRef::name);
}
