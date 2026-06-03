package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.smallrye.mutiny.Uni;

/**
 * A flag whose value is computed dynamically by the provided function.
 */
public class ComputedFlag extends AbstractFlag {

    private final Function<ComputationContext, Uni<Value>> fun;

    public ComputedFlag(String feature, String origin, Map<String, String> metadata,
            Function<ComputationContext, Uni<Value>> fun) {
        super(feature, origin, metadata);
        this.fun = Objects.requireNonNull(fun);
    }

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return fun.apply(context);
    }

}
