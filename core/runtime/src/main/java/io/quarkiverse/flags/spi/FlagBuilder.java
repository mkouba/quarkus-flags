package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.function.Function;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Builder;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.smallrye.mutiny.Uni;

public class FlagBuilder implements Flag.Builder {

    private final String feature;

    private String origin;

    private Map<String, String> metadata = Map.of();

    private Function<ComputationContext, Uni<Value>> fun;

    private Flag.Value value;

    public FlagBuilder(String feature) {
        if (feature == null || feature.isBlank()) {
            throw new IllegalArgumentException("Feature must not be null");
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
    public Builder setComputeAsync(Function<ComputationContext, Uni<Value>> fun) {
        this.fun = fun;
        return this;
    }

    @Override
    public Builder setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public Builder setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public Flag build() {
        String evaluatorId = metadata.get(FlagEvaluator.META_KEY);
        if (evaluatorId != null) {
            ArcContainer container = Arc.container();
            if (container == null) {
                throw new IllegalStateException(
                        "Unable to find the ArC container - flag builder must not be used outside a Quarkus app");
            }
            FlagManager manager = container.instance(FlagManager.class).get();
            FlagEvaluator evaluator = manager.getEvaluator(evaluatorId)
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
