package io.quarkiverse.flags;

import java.math.BigDecimal;

import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Immutable integer flag value.
 */
public final class IntValue implements Flag.Value {

    public static final Uni<Value> createUni(int value) {
        return Uni.createFrom().item(new IntValue(value));
    }

    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public boolean asBoolean() {
        return value == 1;
    }

    @Override
    public String asString() {
        return "" + value;
    }

    @Override
    public int asInt() {
        return value;
    }

    @Override
    public BigDecimal asDecimal() {
        return BigDecimal.valueOf(value);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IntValue other) {
            return value == other.value;
        }
        return false;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

}
