package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.smallrye.mutiny.Uni;

public final class ImmutableFlag implements Flag {

    private final String feature;
    private final Flag.Value state;

    public ImmutableFlag(String feature, Flag.Value state) {
        this.feature = feature;
        this.state = state;
    }

    @Override
    public String feature() {
        return feature;
    }

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return Uni.createFrom().item(state);
    }

}
