package io.quarkiverse.flags.spi;

import java.util.Map;

import io.smallrye.mutiny.Uni;

public abstract class AbstractEvaluatedFlag extends AbstractFlag {

    protected final FlagEvaluator evaluator;

    public AbstractEvaluatedFlag(String feature, Map<String, String> metadata, FlagEvaluator evaluator) {
        super(feature, metadata);
        this.evaluator = evaluator;
    }

    protected abstract Uni<Value> initialValue(ComputationContext context);

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return initialValue(context).chain(value -> evaluator.evaluate(this, value, context));
    }

}
