package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.smallrye.mutiny.Uni;

public class InitializedEvaluatedFlag extends AbstractEvaluatedFlag {

    private final Flag.Value initialValue;

    public InitializedEvaluatedFlag(String feature, String origin, Map<String, String> metadata, Flag.Value initialValue,
            FlagEvaluator evaluator) {
        super(feature, origin, metadata, evaluator);
        this.initialValue = initialValue;
    }

    @Override
    protected Uni<Value> initialValue(ComputationContext context) {
        return Uni.createFrom().item(initialValue);
    }

    @Override
    public String toString() {
        return "InitializedEvaluatedFlag [feature=" + feature() + "]";
    }

}
