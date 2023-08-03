package com.williambl.vampilang.codec;

import com.mojang.serialization.Codec;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.type.VTemplateType;
import com.williambl.vampilang.lang.type.VType;

import java.util.List;
import java.util.Map;

public interface VTypeCodecRegistry {
    Codec<?> rawCodecForType(VType type);
    Map<VType, Codec<?>> allCodecs();
    Codec<VExpression> expressionCodecForType(VType type);
}
