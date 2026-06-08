package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import io.quarkus.arc.impl.LazyValue;
import io.smallrye.mutiny.Uni;

/**
 * Base class for flags that use a {@link FlagEvaluator} to transform the initial value.
 */
public abstract class AbstractEvaluatedFlag extends AbstractFlag {

    protected final LazyValue<FlagEvaluator> evaluator;

    public AbstractEvaluatedFlag(String feature, String origin, Map<String, String> metadata,
            Supplier<FlagEvaluator> evaluatorSupplier) {
        super(feature, origin, metadata);
        this.evaluator = new LazyValue<>(Objects.requireNonNull(evaluatorSupplier));
    }

    public AbstractEvaluatedFlag(String feature, String origin, Map<String, String> metadata,
            FlagEvaluator evaluator) {
        super(feature, origin, metadata);
        Objects.requireNonNull(evaluator);
        this.evaluator = new LazyValue<>(new Supplier<FlagEvaluator>() {
            @Override
            public FlagEvaluator get() {
                return evaluator;
            }
        });
    }

    protected abstract Uni<Value> initialValue(ComputationContext context);

    @Override
    public Uni<Value> compute(ComputationContext context) {
        return initialValue(context).chain(value -> evaluator.get().evaluate(this, value, context));
    }

}
