package io.quarkiverse.flags.runtime;

import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.logging.Logger;

import io.quarkiverse.flags.Feature;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkus.arc.All;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;

@Startup
@ApplicationScoped
public class FlagManagerImpl implements FlagManager {

    private static final Logger LOG = Logger.getLogger(FlagManagerImpl.class);

    private final List<FlagProvider> providers;

    private final Map<String, FlagEvaluator> evaluators;

    private FlagManagerImpl(@All List<FlagProvider> providers,
            @All List<FlagEvaluator> evaluators) {
        List<FlagProvider> sortedProviders = new ArrayList<>();
        int lastPriority = Integer.MAX_VALUE;
        for (FlagProvider provider : providers.stream().sorted(new Comparator<FlagProvider>() {
            @Override
            public int compare(FlagProvider o1, FlagProvider o2) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
        }).toList()) {
            if (provider.getPriority() < lastPriority) {
                sortedProviders.add(provider);
            } else {
                throw new IllegalStateException(
                        "Multiple feature flag providers with the same priority detected: "
                                + providers.stream().map(p -> "\n\t-" + p.getClass().getName() + ":" + p.getPriority())
                                        .collect(Collectors.joining()));
            }
            lastPriority = provider.getPriority();
        }
        this.providers = List.copyOf(sortedProviders);
        Set<String> evaluatorIds = evaluators.stream().map(FlagEvaluator::id).collect(Collectors.toSet());
        if (evaluatorIds.size() != evaluators.size()) {
            throw new IllegalStateException("Multiple flag evaluators with the same id detected:\n"
                    + evaluators.stream().map(e -> "\t-" + e.id() + ": " + e.getClass().getName())
                            .collect(Collectors.joining("\n")));
        }
        this.evaluators = evaluators.stream().collect(toMap(FlagEvaluator::id, Function.identity()));
    }

    @Override
    public Uni<List<Flag>> findAll() {
        if (providers.isEmpty()) {
            return Uni.createFrom().item(List.of());
        }
        ConcurrentMap<String, Flag> ret = new ConcurrentHashMap<>();
        Iterator<FlagProvider> it = providers.iterator();
        FlagProvider first = it.next();
        AtomicReference<String> providerClassName = new AtomicReference<String>(first.getClass().getName());
        Uni<Collection<Flag>> uni = first.getFlags();
        while (it.hasNext()) {
            FlagProvider next = it.next();
            uni = uni.chain(c -> {
                addFlags(providerClassName.get(), c, ret);
                providerClassName.set(next.getClass().getName());
                return next.getFlags();
            });
        }
        return uni.map(c -> {
            addFlags(providerClassName.get(), c, ret);
            return List.copyOf(ret.values());
        });

    }

    private void addFlags(String providerClassName, Collection<Flag> flags, ConcurrentMap<String, Flag> result) {
        for (Flag flag : flags) {
            if (result.putIfAbsent(flag.feature(), new DelegatingFlag(flag, providerClassName)) != null) {
                LOG.debugf(
                        "Flag with feature %s from provider %s is ignored: a flag with the same feature is declared by a provider with higher priority",
                        flag.feature(), providerClassName);
            }
        }
    }

    @Override
    public Uni<Optional<Flag>> find(String feature) {
        return findAll().map(flags -> flags.stream()
                .filter(f -> f.feature().equals(feature))
                .findFirst());
    }

    @Override
    public Optional<FlagEvaluator> getEvaluator(String id) {
        FlagEvaluator evaluator = evaluators.get(id);
        return Optional.ofNullable(evaluator);
    }

    @Feature("")
    @Produces
    Flag produceFlag(InjectionPoint injectionPoint) {
        Feature feature = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(Feature.class)) {
                feature = (Feature) qualifier;
            }
        }
        if (feature == null) {
            // This should never happen
            throw new IllegalStateException("Injection point does not declare @Feature");
        }
        return new InjectedFlag(feature.value());
    }

    public List<FlagProvider> getProviders() {
        return providers;
    }

    public Collection<FlagEvaluator> getEvaluators() {
        return evaluators.values();
    }

    class InjectedFlag implements Flag {

        private final String feature;

        private InjectedFlag(String feature) {
            this.feature = feature;
        }

        @Override
        public String feature() {
            return feature;
        }

        @Override
        public String origin() {
            return findAndAwait(feature).orElseThrow().origin();
        }

        @Override
        public Uni<Value> compute(ComputationContext context) {
            return find(feature).chain(f -> f.orElseThrow().compute(context));
        }

        @Override
        public Map<String, String> metadata() {
            return findAndAwait(feature).orElseThrow().metadata();
        }

    }

    class DelegatingFlag implements Flag {

        private final Flag delegate;

        private final String providerClassName;

        DelegatingFlag(Flag delegate, String providerClassName) {
            this.delegate = delegate;
            this.providerClassName = providerClassName;
        }

        @Override
        public String feature() {
            return delegate.feature();
        }

        @Override
        public String origin() {
            String origin = delegate.origin();
            return origin != null ? origin : providerClassName;
        }

        @Override
        public Map<String, String> metadata() {
            return delegate.metadata();
        }

        @Override
        public Uni<Value> compute(ComputationContext computationContext) {
            return delegate.compute(computationContext);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

    }

}
