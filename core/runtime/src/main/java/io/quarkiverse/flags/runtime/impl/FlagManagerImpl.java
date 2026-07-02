package io.quarkiverse.flags.runtime.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.jboss.logging.Logger;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.FlagCache;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkus.arc.All;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;

@Startup
@ApplicationScoped // Must be normal scoped so that providers/evaluators can inject it
public class FlagManagerImpl implements FlagManager {

    private static final Logger LOG = Logger.getLogger(FlagManagerImpl.class);

    // See io.quarkiverse.flags.spi.ComponentOrder
    private final List<FlagProviderWithId> providers;

    // id -> evaluator
    private final Map<String, FlagEvaluator> evaluators;

    // null if caching is not enabled or no FlagCache bean is available
    private final FlagCache cache;

    private final FlagsCacheConfig cacheConfig;

    FlagManagerImpl(@All List<InstanceHandle<FlagProvider>> providerHandles,
            @All List<InstanceHandle<FlagEvaluator>> evaluatorHandles,
            FlagContext context,
            FlagsCacheConfig cacheConfig,
            @Any Instance<FlagCache> cacheInstance) {
        // Build provider ID map from @Identifier qualifiers
        Map<String, FlagProvider> providerById = new LinkedHashMap<>();
        for (InstanceHandle<FlagProvider> handle : providerHandles) {
            FlagProvider provider = handle.get();
            String id = readIdentifier(handle);
            providerById.put(id, provider);
        }
        // Sort providers according to the build-time validated order
        List<FlagProviderWithId> sortedProviders = new ArrayList<>(providerById.size());
        for (String id : context.getOrderedProviderIds()) {
            FlagProvider provider = providerById.get(id);
            if (provider != null) {
                sortedProviders.add(new FlagProviderWithId(provider, id));
            }
        }
        this.providers = List.copyOf(sortedProviders);
        // Build evaluator map from @Identifier qualifiers
        Map<String, FlagEvaluator> evaluatorMap = new LinkedHashMap<>();
        for (InstanceHandle<FlagEvaluator> handle : evaluatorHandles) {
            FlagEvaluator evaluator = handle.get();
            String id = readIdentifier(handle);
            evaluatorMap.put(id, evaluator);
        }
        this.evaluators = Map.copyOf(evaluatorMap);
        if (cacheConfig.enabled() && !cacheInstance.isUnsatisfied()) {
            if (cacheInstance.isAmbiguous()) {
                LOG.warnf("Multiple FlagCache beans found, caching is disabled");
                this.cache = null;
            } else {
                this.cache = cacheInstance.get();
            }
        } else {
            this.cache = null;
        }
        this.cacheConfig = cacheConfig;
    }

    @Override
    public Uni<List<Flag>> findAll() {
        if (providers.isEmpty()) {
            return Uni.createFrom().item(List.of());
        }
        ConcurrentMap<String, Flag> ret = new ConcurrentHashMap<>();
        Iterator<FlagProviderWithId> it = providers.iterator();
        FlagProviderWithId first = it.next();
        Uni<Collection<Flag>> uni = getFlags(first);
        while (it.hasNext()) {
            FlagProviderWithId next = it.next();
            uni = uni.chain(c -> {
                addFlags(c, ret);
                return getFlags(next);
            });
        }
        return uni.map(c -> {
            addFlags(c, ret);
            return List.copyOf(ret.values());
        });

    }

    private void addFlags(Collection<Flag> flags, ConcurrentMap<String, Flag> result) {
        for (Flag flag : flags) {
            if (result.putIfAbsent(flag.feature(), flag) != null) {
                LOG.debugf(
                        "Flag with feature %s from provider %s is ignored: a flag with the same feature is declared by a provider with higher priority",
                        flag.feature(), flag.origin());
            }
        }
    }

    @Override
    public Uni<Optional<Flag>> find(String feature) {
        if (feature == null || feature.isBlank()) {
            throw new IllegalArgumentException("Feature name must not be null or blank");
        }
        if (providers.isEmpty()) {
            return Uni.createFrom().item(Optional.empty());
        }
        return findInProviders(feature, providers.iterator());
    }

    private Uni<Collection<Flag>> getFlags(FlagProviderWithId p) {
        if (cache != null && isCachingEnabled(p)) {
            return cache.getOrComputeFlags(p.id(), () -> p.provider().getFlags());
        }
        return p.provider().getFlags();
    }

    private Uni<Flag> getFlag(FlagProviderWithId p, String feature) {
        if (cache != null && isCachingEnabled(p)) {
            return cache.getOrComputeFlag(p.id(), feature, () -> p.provider().getFlag(feature));
        }
        return p.provider().getFlag(feature);
    }

    private boolean isCachingEnabled(FlagProviderWithId p) {
        FlagsCacheConfig.ProviderCacheConfig providerConfig = cacheConfig.providers().get(p.id());
        if (providerConfig != null) {
            return providerConfig.enabled();
        }
        return p.provider().isCacheable();
    }

    private Uni<Optional<Flag>> findInProviders(String feature, Iterator<FlagProviderWithId> it) {
        FlagProviderWithId p = it.next();
        return getFlag(p, feature).chain(f -> {
            if (f != null) {
                return Uni.createFrom().item(Optional.of(f));
            }
            if (it.hasNext()) {
                return findInProviders(feature, it);
            }
            return Uni.createFrom().item(Optional.empty());
        });
    }

    @Override
    public Optional<FlagEvaluator> getEvaluator(String id) {
        FlagEvaluator evaluator = evaluators.get(id);
        return Optional.ofNullable(evaluator);
    }

    public List<FlagProviderWithId> getProviders() {
        return providers;
    }

    public Map<String, FlagEvaluator> getEvaluators() {
        return evaluators;
    }

    private static String readIdentifier(InstanceHandle<?> handle) {
        for (Annotation qualifier : handle.getBean().getQualifiers()) {
            if (qualifier instanceof Identifier identifier) {
                return identifier.value();
            }
        }
        throw new IllegalStateException(
                "Bean " + handle.getBean().getBeanClass() + " is missing @Identifier qualifier");
    }

    public record FlagProviderWithId(FlagProvider provider, String id) {
    }

}
