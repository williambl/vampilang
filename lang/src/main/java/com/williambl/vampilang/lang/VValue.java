package com.williambl.vampilang.lang;

import com.williambl.vampilang.lang.type.TypedVType;
import com.williambl.vampilang.lang.type.VType;

import java.util.NoSuchElementException;
import java.util.Objects;

public record VValue(VType type, Object value) {
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
