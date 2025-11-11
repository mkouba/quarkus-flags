package io.quarkiverse.flags.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import jakarta.enterprise.event.Event;
import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkiverse.flags.spi.AbstractEvaluatedFlag;
import io.quarkiverse.flags.spi.AbstractFlag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkiverse.flags.spi.FlagProvider;
import io.smallrye.mutiny.Uni;

@Singleton
public class InMemoryFlagProviderImpl extends AbstractFlagProvider implements InMemoryFlagProvider {

    private final ConcurrentMap<String, Flag> flags = new ConcurrentHashMap<>();

    private final Event<FlagAdded> flagAdded;

    private final Event<FlagRemoved> flagRemoved;

    public InMemoryFlagProviderImpl(FlagManager manager, Event<FlagAdded> flagAdded, Event<FlagRemoved> flagRemoved) {
        super(manager);
        this.flagAdded = flagAdded;
        this.flagRemoved = flagRemoved;
    }

    @Override
    public int getPriority() {
        return FlagProvider.DEFAULT_PRIORITY + 3;
    }

    @Override
    public Iterable<Flag> getFlags() {
        return flags.values();
    }

    @Override
    public FlagDefinition newFlag(String feature) {
        return new FlagDefinitionImpl(feature);
    }

    @Override
    public Flag removeFlag(String feature) {
        Flag removed = flags.remove(feature);
        if (removed != null) {
            flagRemoved.fire(new FlagRemoved(removed));
        }
        return removed;
    }

    class FlagDefinitionImpl implements FlagDefinition {

        private final String feature;

        private Map<String, String> metadata = Map.of();

        private Function<ComputationContext, Uni<Value>> fun;

        FlagDefinitionImpl(String feature) {
            this.feature = feature;
        }

        @Override
        public FlagDefinition setComputeAsync(Function<ComputationContext, Uni<Value>> fun) {
            this.fun = fun;
            return this;
        }

        @Override
        public FlagDefinition setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public Flag register() {
            Flag newFlag;
            String evaluatorId = metadata.get(FlagEvaluator.META_KEY);
            if (evaluatorId != null) {
                FlagEvaluator evaluator = manager.getEvaluator(evaluatorId).orElseThrow();
                newFlag = new InMemoryEvaluatedFlag(feature, metadata, fun, evaluator);
            } else {
                newFlag = new InMemoryFlag(feature, metadata, fun);
            }
            Flag existing = flags.putIfAbsent(feature, newFlag);
            if (existing == null) {
                flagAdded.fire(new FlagAdded(newFlag));
                return newFlag;
            }
            return null;
        }

    }

    class InMemoryFlag extends AbstractFlag {

        private final Function<ComputationContext, Uni<Value>> fun;

        InMemoryFlag(String feature, Map<String, String> metadata, Function<ComputationContext, Uni<Value>> fun) {
            super(feature, metadata);
            this.fun = fun;
        }

        @Override
        public Uni<Value> compute(ComputationContext context) {
            return fun.apply(context);
        }

    }

    class InMemoryEvaluatedFlag extends AbstractEvaluatedFlag {

        private final Function<ComputationContext, Uni<Value>> fun;

        InMemoryEvaluatedFlag(String feature, Map<String, String> metadata, Function<ComputationContext, Uni<Value>> fun,
                FlagEvaluator evaluator) {
            super(feature, metadata, evaluator);
            this.fun = fun;
        }

        @Override
        protected Uni<Value> initialValue(ComputationContext context) {
            return fun.apply(context);
        }

    }

}
