package com.williambl.vampilang.lang.type;

import com.google.common.reflect.TypeToken;
import com.williambl.vampilang.lang.VValue;

import java.util.Map;
import java.util.function.Function;

public final class ConstructableVType<T> extends TypedVType<T> {
    public final Function<Map<String, VValue>, T> constructor;

    ConstructableVType(TypeToken<T> type, Function<Map<String, VValue>, T> constructor) {
        super(type);
        this.constructor = constructor;
    }
}
