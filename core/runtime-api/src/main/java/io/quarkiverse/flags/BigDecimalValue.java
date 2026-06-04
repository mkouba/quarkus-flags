package io.quarkiverse.flags;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
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
        if (value.compareTo(BigDecimal.ONE) == 0) {
            return true;
        } else if (value.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        throw new NoSuchElementException("Decimal value [" + value + "] cannot be converted to boolean");
    }

    @Override
    public String asString() {
        return value.toString();
    }

    @Override
    public int asInt() {
        try {
            return value.intValueExact();
        } catch (ArithmeticException e) {
            throw new NoSuchElementException("Decimal value [" + value + "] cannot be converted to int", e);
        }
    }

    @Override
    public BigDecimal asDecimal() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
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
