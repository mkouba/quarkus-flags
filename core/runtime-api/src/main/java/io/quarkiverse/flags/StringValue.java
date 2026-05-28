package io.quarkiverse.flags;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Objects;

import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Immutable string flag value.
 */
public final class StringValue implements Flag.Value {

    public static final Uni<Value> createUni(String value) {
        return Uni.createFrom().item(new StringValue(value));
    }

    private final String value;

    public StringValue(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public int asInt() {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public BigDecimal asDecimal() {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new NoSuchElementException();
        }
    }

}
