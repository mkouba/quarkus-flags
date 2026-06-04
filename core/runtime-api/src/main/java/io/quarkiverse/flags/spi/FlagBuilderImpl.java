package io.quarkiverse.flags.spi;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

import io.quarkiverse.flags.BigDecimalValue;
import io.quarkiverse.flags.BooleanValue;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Builder;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.IntValue;
import io.quarkiverse.flags.StringValue;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.smallrye.mutiny.Uni;

/**
 * Default {@link Flag.Builder} implementation. Not thread-safe — instances must not be shared across threads.
 */
public class FlagBuilderImpl implements Flag.Builder {

    private final String feature;

    private String origin;

    private Map<String, String> metadata = Map.of();

    private Function<ComputationContext, Uni<Value>> fun;

    private Flag.Value value;

    private FlagManager manager;

    public FlagBuilderImpl(String feature) {
        if (feature == null || feature.isBlank()) {
            throw new IllegalArgumentException("Feature must not be null or blank");
        }
        this.feature = feature;
    }

    @Override
    public Builder setEnabled(boolean value) {
        this.value = BooleanValue.from(value);
        return this;
    }

    @Override
    public Builder setString(String value) {
        this.value = new StringValue(value);
        return this;
    }

    @Override
    public Builder setInt(int value) {
        this.value = new IntValue(value);
        return this;
    }

    @Override
    public Builder setDecimal(BigDecimal value) {
        this.value = new BigDecimalValue(value);
        return this;
    }

    @Override
    public Builder setComputeAsync(Function<ComputationContext, Uni<Value>> fun) {
        this.fun = fun;
        return this;
    }

    @Override
    public Builder setMetadata(Map<String, String> metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata must not be null");
        }
        this.metadata = metadata;
        return this;
    }

    @Override
    public Builder setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public Builder setFlagManager(FlagManager manager) {
        this.manager = manager;
        return this;
    }

    @Override
    public Flag build() {
        if (origin == null || origin.isBlank()) {
            throw new IllegalStateException("Origin must be set");
        }
        if (value == null && fun == null) {
            value = BooleanValue.TRUE;
        }
        String evaluatorId = metadata.get(FlagEvaluator.META_KEY);
        if (evaluatorId != null) {
            FlagManager flagManager;
            if (manager != null) {
                flagManager = manager;
            } else {
                ArcContainer container = Arc.container();
                if (container == null) {
                    throw new IllegalStateException(
                            "Unable to find the ArC container - flag builder must not be used outside a Quarkus app");
                }
                flagManager = container.instance(FlagManager.class).get();
            }
            FlagEvaluator evaluator = flagManager.getEvaluator(evaluatorId)
                    .orElseThrow(() -> new IllegalStateException("Flag evaluator does not exist: " + evaluatorId));
            return value != null ? new InitializedEvaluatedFlag(feature, origin, metadata, value, evaluator)
                    : new ComputedEvaluatedFlag(feature, origin, metadata, evaluator, fun);
        }
        if (value != null) {
            return new ImmutableFlag(feature, origin, metadata, value);
        }
        return new ComputedFlag(feature, origin, metadata, fun);
    }

}
