package io.quarkiverse.flags;

import java.math.BigDecimal;

import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Immutable boolean flag value.
 */
public final class BooleanValue implements Flag.Value {

    public static final Uni<Value> createUni(boolean value) {
        return Uni.createFrom().item(value ? TRUE : FALSE);
    }

    public static final BooleanValue from(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static final BooleanValue TRUE = new BooleanValue(true);
    public static final BooleanValue FALSE = new BooleanValue(false);

    private final boolean value;

    private BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public String asString() {
        return "" + value;
    }

    @Override
    public int asInt() {
        return value ? 1 : 0;
    }

    @Override
    public BigDecimal asDecimal() {
        return value ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BooleanValue other) {
            return value == other.value;
        }
        return false;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

}
