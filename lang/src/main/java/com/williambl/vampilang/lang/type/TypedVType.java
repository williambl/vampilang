package com.williambl.vampilang.lang.type;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

public final class TypedVType<T> extends SimpleVType {
    public final TypeToken<T> type;

    TypedVType(TypeToken<T> type) {
        super(obj -> type.isSupertypeOf(obj.getClass()));
        this.type = type;
    }
}
