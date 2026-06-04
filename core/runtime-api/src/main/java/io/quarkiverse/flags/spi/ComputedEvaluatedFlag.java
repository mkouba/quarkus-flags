package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.smallrye.mutiny.Uni;

/**
 * A flag whose initial value is computed dynamically and then transformed by a {@link FlagEvaluator}.
 */
public class ComputedEvaluatedFlag extends AbstractEvaluatedFlag {

    private final Function<ComputationContext, Uni<Value>> fun;

    public ComputedEvaluatedFlag(String feature, String origin, Map<String, String> metadata, FlagEvaluator evaluator,
            Function<ComputationContext, Uni<Value>> fun) {
        super(feature, origin, metadata, evaluator);
        this.fun = Objects.requireNonNull(fun);
    }

    @Override
    protected Uni<Value> initialValue(ComputationContext context) {
        return fun.apply(context);
    }

    @Override
    public String toString() {
        return "ComputedEvaluatedFlag [feature=" + feature() + "]";
    }

}
