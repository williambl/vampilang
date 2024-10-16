package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.type.TypedVType;
import com.williambl.vampilang.lang.type.VType;

import java.util.NoSuchElementException;
import java.util.Objects;

public record VValue(VType type, Object value) {
    public static <T> VValue value(VType type, T obj, VEnvironment env) {
        return new VValue(type, obj, env);
    }

    public VValue(VType type, Object value, VEnvironment env) {
        this(type, value);
        if (!this.type.accepts(this.value, env)) {
            throw new IllegalArgumentException("Type %s does not accept value %s!".formatted(this.type, this.value));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(TypedVType<T> type) {
        if (this.type.equals(type)) {
            return (T) this.value;
        }

        throw new NoSuchElementException();
    }

    @SuppressWarnings("unchecked")
    public <T> T getUnchecked() {
        return (T) this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        VValue vValue = (VValue) o;
        return Objects.equals(this.type, vValue.type) && Objects.equals(this.value, vValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.value);
    }
}
