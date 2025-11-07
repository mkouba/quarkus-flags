package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.smallrye.mutiny.Uni;

public class EvaluatedFlag implements Flag {

    private final String feature;

    private final Map<String, String> metadata;

    private final Flag.Value initialValue;

    private final FlagEvaluator evaluator;

    public EvaluatedFlag(String feature, Map<String, String> metadata, Flag.Value initialValue, FlagEvaluator evaluator) {
        this.feature = feature;
        this.metadata = metadata;
        this.initialValue = initialValue;
        this.evaluator = evaluator;
    }

    @Override
    public String feature() {
        return feature;
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return evaluator.evaluate(this, initialValue, context);
    }

}
