package io.quarkiverse.flags.cache;

import java.util.Collection;
import java.util.function.Supplier;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.FlagCache;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheManager;
import io.smallrye.mutiny.Uni;

/**
 * {@link FlagCache} implementation backed by the Quarkus Cache extension.
 * <p>
 * Uses one cache per provider, named {@value #CACHE_NAME_PREFIX}{@code providerId}. Within each cache, the all-flags
 * entry is stored under the {@value #ALL_FLAGS_KEY} key and individual flag entries are stored under their feature name.
 * <p>
 * Unlike the built-in in-memory implementation, this cache does not fall back from individual flag lookups to the
 * all-flags entry because nested cache access on the same cache instance may cause issues with certain cache providers.
 * <p>
 * TTL and eviction are managed by the underlying cache provider (e.g. Caffeine) and can be configured per provider via
 * standard Quarkus Cache configuration properties, for example:
 * {@code quarkus.cache.caffeine."quarkus-flags.my-provider".expire-after-write=30s}.
 *
 * @see CacheManager
 */
@Singleton
public class CacheFlagCache implements FlagCache {

    static final String CACHE_NAME_PREFIX = "quarkus-flags.";

    static final String ALL_FLAGS_KEY = "__all";

    private final CacheManager cacheManager;

    CacheFlagCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Uni<Collection<Flag>> getOrComputeFlags(String providerId, Supplier<Uni<Collection<Flag>>> loader) {
        return getCache(providerId).getAsync(ALL_FLAGS_KEY, k -> loader.get());
    }

    @Override
    public Uni<Flag> getOrComputeFlag(String providerId, String feature, Supplier<Uni<Flag>> loader) {
        return getCache(providerId).getAsync(feature, k -> loader.get());
    }

    @Override
    public Uni<Void> invalidate(String providerId) {
        return getCache(providerId).invalidateAll();
    }

    @Override
    public Uni<Void> invalidateAll() {
        return Uni.join().all(
                cacheManager.getCacheNames().stream()
                        .filter(name -> name.startsWith(CACHE_NAME_PREFIX))
                        .map(name -> cacheManager.getCache(name).orElseThrow().invalidateAll())
                        .toList())
                .andFailFast()
                .replaceWithVoid();
    }

    private Cache getCache(String providerId) {
        return cacheManager.getCache(cacheName(providerId))
                .orElseThrow(() -> new IllegalStateException("Cache not found for provider: " + providerId));
    }

    public static String cacheName(String providerId) {
        return CACHE_NAME_PREFIX + providerId;
    }

}
