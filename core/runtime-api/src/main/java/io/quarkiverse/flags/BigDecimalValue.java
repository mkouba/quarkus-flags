package io.quarkiverse.flags;

import java.math.BigDecimal;
import java.util.Objects;

import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Immutable decimal flag value.
 */
public final class BigDecimalValue implements Flag.Value {

    public static final Uni<Value> createUni(BigDecimal value) {
        return Uni.createFrom().item(new BigDecimalValue(value));
    }

    private final BigDecimal value;

    public BigDecimalValue(BigDecimal value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public boolean asBoolean() {
        return value.compareTo(BigDecimal.ONE) == 0;
    }

    @Override
    public String asString() {
        return value.toString();
    }

    @Override
    public int asInt() {
        return value.intValue();
    }

    @Override
    public BigDecimal asDecimal() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BigDecimalValue other) {
            return value.compareTo(other.value) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
