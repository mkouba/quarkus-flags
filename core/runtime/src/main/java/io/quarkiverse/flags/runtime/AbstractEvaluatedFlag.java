package io.quarkiverse.flags.runtime;

import java.util.Map;

import io.quarkiverse.flags.spi.FlagEvaluator;
import io.smallrye.mutiny.Uni;

public abstract class AbstractEvaluatedFlag extends AbstractFlag {

    protected final FlagEvaluator evaluator;

    public AbstractEvaluatedFlag(String feature, String origin, Map<String, String> metadata, FlagEvaluator evaluator) {
        super(feature, origin, metadata);
        this.evaluator = evaluator;
    }

    protected abstract Uni<Value> initialValue(ComputationContext context);

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return initialValue(context).chain(value -> evaluator.evaluate(this, value, context));
    }

}
