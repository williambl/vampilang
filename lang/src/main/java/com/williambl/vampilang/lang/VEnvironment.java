package com.williambl.vampilang.lang;

import com.mojang.serialization.Codec;
import com.williambl.vampilang.lang.function.VFunctionDefinition;
import com.williambl.vampilang.lang.type.SimpleVType;
import com.williambl.vampilang.lang.type.VParameterisedType;
import com.williambl.vampilang.lang.type.VType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface VEnvironment {
    Codec<?> rawCodecForType(VType type);
    Map<VType, Codec<?>> codecsMatching(VType type);
    Codec<VExpression> expressionCodecForType(VType type, EvaluationContext.Spec spec);
    void registerCodecForType(VType type, Codec<?> codec);
    void registerCodecForParameterisedType(SimpleVType bareType, Function<VParameterisedType, Codec<?>> codecForType);
    void registerType(String name, VType type);
    VType getType(String typeName);
    VParameterisedType listType();
    Map<String, VType> allTypes();
    void registerFunction(VFunctionDefinition function);
    TypeNamer createTypeNamer();
    default void registerType(String name, VType type, Codec<?> codec) {
        this.registerType(name, type);
        this.registerCodecForType(type, codec);
    }
}
