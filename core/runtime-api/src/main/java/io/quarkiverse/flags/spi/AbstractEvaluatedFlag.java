package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.Objects;

import io.smallrye.mutiny.Uni;

/**
 * Base class for flags that use a {@link FlagEvaluator} to transform the initial value.
 */
public abstract class AbstractEvaluatedFlag extends AbstractFlag {

    protected final FlagEvaluator evaluator;

    public AbstractEvaluatedFlag(String feature, String origin, Map<String, String> metadata, FlagEvaluator evaluator) {
        super(feature, origin, metadata);
        this.evaluator = Objects.requireNonNull(evaluator);
    }

    protected abstract Uni<Value> initialValue(ComputationContext context);

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return initialValue(context).chain(value -> evaluator.evaluate(this, value, context));
    }

}
