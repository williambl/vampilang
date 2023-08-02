package com.williambl.vampilang.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.williambl.vampilang.lang.VExpression;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.VType;

public class VExpressionCodecs {
    public Codec<VExpression.FunctionApplication> createFunctionApplicationCodec(VFunctionDefinition functionDefinition) {
    }

    public <T> Codec<T> vTypeCodec(VType type) {
        return null; //TODO
    }
}
