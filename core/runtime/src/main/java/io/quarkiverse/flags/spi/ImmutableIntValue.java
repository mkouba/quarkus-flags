package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;

public class ImmutableIntValue implements Flag.Value {

    private final int value;

    public ImmutableIntValue(int value) {
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

}
