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
        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            return true;
        } else if (value.equalsIgnoreCase("false") || value.equals("0")) {
            return false;
        }
        throw new NoSuchElementException();
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
