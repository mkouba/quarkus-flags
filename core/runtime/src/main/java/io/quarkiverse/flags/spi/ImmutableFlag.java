package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.smallrye.mutiny.Uni;

public final class ImmutableFlag implements Flag {

    private final String feature;
    private final Flag.State state;

    public ImmutableFlag(String feature, boolean enabled) {
        this.feature = feature;
        this.state = enabled ? ImmutableFlagState.ON : ImmutableFlagState.OFF;
    }

    @Override
    public String feature() {
        return feature;
    }

    @Override
    public Uni<State> compute(ComputationContext context) {
        return Uni.createFrom().item(state);
    }

}
