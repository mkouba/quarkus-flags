package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.smallrye.mutiny.Uni;

public class EvaluatedFlag extends AbstractFlag {

    private final Flag.Value initialValue;

    private final FlagEvaluator evaluator;

    public EvaluatedFlag(String feature, Map<String, String> metadata, Flag.Value initialValue, FlagEvaluator evaluator) {
        super(feature, metadata);
        this.initialValue = initialValue;
        this.evaluator = evaluator;
    }

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return evaluator.evaluate(this, initialValue, context);
    }

    @Override
    public String toString() {
        return "EvaluatedFlag [feature=" + feature() + "]";
    }

}
