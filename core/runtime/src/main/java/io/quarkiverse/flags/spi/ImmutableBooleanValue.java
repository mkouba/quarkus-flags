package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;

public class ImmutableBooleanValue implements Flag.Value {

    public static final ImmutableBooleanValue from(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static final ImmutableBooleanValue TRUE = new ImmutableBooleanValue(true);
    public static final ImmutableBooleanValue FALSE = new ImmutableBooleanValue(false);

    private final boolean value;

    private ImmutableBooleanValue(boolean value) {
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

}
