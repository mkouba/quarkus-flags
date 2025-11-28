package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.function.Function;

import io.smallrye.mutiny.Uni;

public class ComputedEvaluatedFlag extends AbstractEvaluatedFlag {

    private final Function<ComputationContext, Uni<Value>> fun;

    public ComputedEvaluatedFlag(String feature, String origin, Map<String, String> metadata, FlagEvaluator evaluator,
            Function<ComputationContext, Uni<Value>> fun) {
        super(feature, origin, metadata, evaluator);
        this.fun = fun;
    }

    @Override
    protected Uni<Value> initialValue(ComputationContext context) {
        return fun.apply(context);
    }

}
