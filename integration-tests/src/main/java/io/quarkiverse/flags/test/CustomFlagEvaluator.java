package io.quarkiverse.flags.test;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.smallrye.mutiny.Uni;

@Singleton
public class CustomFlagEvaluator implements FlagEvaluator {

    @Override
    public String id() {
        return "custom";
    }

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        return Uni.createFrom().item(initialValue);
    }

}
