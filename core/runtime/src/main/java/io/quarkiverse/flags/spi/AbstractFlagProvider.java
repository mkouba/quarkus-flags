package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;

public abstract class AbstractFlagProvider implements FlagProvider {

    protected final FlagManager manager;

    public AbstractFlagProvider(FlagManager manager) {
        this.manager = manager;
    }

    protected Flag createFlag(String feature, Flag.Value initialValue, Map<String, String> metadata) {
        String evaluatorId = metadata.get(FlagEvaluator.META_KEY);
        if (evaluatorId != null) {
            FlagEvaluator evaluator = manager.getEvaluator(evaluatorId)
                    .orElseThrow(() -> new IllegalStateException("Flag evaluator does not exist: " + evaluatorId));
            return new InitializedEvaluatedFlag(feature, metadata, initialValue, evaluator);
        } else {
            return new ImmutableFlag(feature, metadata, initialValue);
        }
    }

}
