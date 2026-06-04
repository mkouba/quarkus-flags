package io.quarkiverse.flags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagManager;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag with the specified sub-evaluators executed in the given order.
 * <p>
 * The evaluator is configured through the {@link Flag#metadata()}. The value of {@value #SUB_EVALUATORS} represents a
 * comma-separated list of sub-evaluator identifiers.
 */
@Identifier(CompositeFlagEvaluator.ID)
@Singleton
public class CompositeFlagEvaluator implements FlagEvaluator {

    public static final String ID = "quarkus.composite";

    /**
     * Comma-separated list of evaluator identifiers.
     */
    public static final String SUB_EVALUATORS = "sub-evaluators";

    @Inject
    FlagManager flagManager;

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        Uni<Value> value = Uni.createFrom().item(initialValue);
        if (initialValue != null && initialValue.asBoolean(false)) {
            String subEvaluators = flag.metadata().get(SUB_EVALUATORS);
            if (subEvaluators != null && !subEvaluators.isBlank()) {
                String[] ids = subEvaluators.split(",");
                List<FlagEvaluator> all = new ArrayList<>(ids.length);
                for (String id : ids) {
                    String strippedId = id.strip();
                    all.add(flagManager.getEvaluator(strippedId).orElseThrow(
                            () -> new IllegalStateException(
                                    "Flag evaluator with id [%s] not found".formatted(strippedId))));
                }
                return doEvaluate(flag, value, all.iterator(), computationContext);
            }
        }
        return value;
    }

    private Uni<Value> doEvaluate(Flag flag, Uni<Value> lastValue, Iterator<FlagEvaluator> it,
            ComputationContext computationContext) {
        if (it.hasNext()) {
            FlagEvaluator next = it.next();
            return lastValue.chain(v -> {
                Uni<Value> nextValue = next.evaluate(flag, v, computationContext);
                return doEvaluate(flag, nextValue, it, computationContext);
            });
        }
        return lastValue;
    }

}
