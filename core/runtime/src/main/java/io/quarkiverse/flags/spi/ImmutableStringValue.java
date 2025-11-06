package io.quarkiverse.flags.spi;

import java.util.NoSuchElementException;

import io.quarkiverse.flags.Flag;

public class ImmutableStringValue implements Flag.Value {

    private final String value;

    public ImmutableStringValue(String value) {
        this.value = value;
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

}
