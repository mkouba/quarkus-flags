package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.smallrye.mutiny.Uni;

public final class ImmutableFlag extends AbstractFlag {

    private final Flag.Value value;

    public ImmutableFlag(String feature, Flag.Value value) {
        this(feature, null, Map.of(), value);
    }

    public ImmutableFlag(String feature, String origin, Map<String, String> metadata, Value value) {
        super(feature, origin, metadata);
        this.value = value;
    }

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return Uni.createFrom().item(value);
    }

    @Override
    public String toString() {
        return "ImmutableFlag [feature=" + feature() + "]";
    }

}
