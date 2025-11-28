package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Immutable integer flag value.
 */
public class IntValue implements Flag.Value {

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

}
