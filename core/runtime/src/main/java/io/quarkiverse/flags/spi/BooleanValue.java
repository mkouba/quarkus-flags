package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Immutable boolean flag value.
 */
public class BooleanValue implements Flag.Value {

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

}
