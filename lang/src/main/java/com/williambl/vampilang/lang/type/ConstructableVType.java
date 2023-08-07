package com.williambl.vampilang.lang.type;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.VValue;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.function.Function;

public final class ConstructableVType<T> extends TypedVType<T> {
    public final Function<Map<String, VValue>, T> constructor;
    public final @Unmodifiable Map<String, VType> propertyTypes;

    ConstructableVType(TypeToken<T> type, Function<Map<String, VValue>, T> constructor, @Unmodifiable Map<String, VType> propertyTypes) {
        super(type);
        this.constructor = constructor;
        this.propertyTypes = propertyTypes;
    }
}
