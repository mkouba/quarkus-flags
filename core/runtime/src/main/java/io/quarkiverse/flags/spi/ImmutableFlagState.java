package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;

public class ImmutableFlagState implements Flag.State {

    public static final ImmutableFlagState from(boolean enabled) {
        return enabled ? ON : OFF;
    }

    public static final ImmutableFlagState ON = new ImmutableFlagState(true);
    public static final ImmutableFlagState OFF = new ImmutableFlagState(false);

    private final boolean enabled;

    private ImmutableFlagState(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isOn() {
        return enabled;
    }

}
