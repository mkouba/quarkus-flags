package io.quarkiverse.flags.runtime.impl;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.FlagCache;
import io.quarkus.arc.DefaultBean;
import io.smallrye.mutiny.Uni;

/**
 * In-memory {@link FlagCache} implementation backed by a {@link ConcurrentHashMap}.
 * <p>
 * Each provider has a single {@link CacheEntry} that holds both the all-flags result and individually cached flags. The
 * entire entry is discarded when the provider-level TTL expires.
 * <p>
 * The TTL is configurable globally via {@code quarkus.flags.cache.ttl} (default 10 minutes) and per-provider via
 * {@code quarkus.flags.cache."provider-id".ttl}. Caching can be disabled for a specific provider via
 * {@code quarkus.flags.cache."provider-id".enabled=false}.
 *
 * @see FlagsCacheConfig
 */
@DefaultBean
@Singleton
public class InMemoryFlagCache implements FlagCache {

    private final ConcurrentMap<String, CacheEntry> cache;

    private final FlagsCacheConfig config;

    InMemoryFlagCache(FlagsCacheConfig config) {
        this.config = config;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Uni<Collection<Flag>> getOrComputeFlags(String providerId, Supplier<Uni<Collection<Flag>>> loader) {
        CacheEntry entry = getValidEntry(providerId);
        Uni<Collection<Flag>> existing = entry.allFlags.get();
        if (existing != null) {
            return existing;
        }
        Uni<Collection<Flag>> value = loader.get().memoize().indefinitely();
        // CAS ensures only one thread wins; losers discard their Uni (never subscribed, no wasted provider call)
        entry.allFlags.compareAndSet(null, value);
        return entry.allFlags.get();
    }

    @Override
    public Uni<Flag> getOrComputeFlag(String providerId, String feature, Supplier<Uni<Flag>> loader) {
        CacheEntry entry = getValidEntry(providerId);
        Uni<Flag> existing = entry.flags.get(feature);
        if (existing != null) {
            return existing;
        }
        // Fall back to allFlags if populated (e.g. by a prior findAll() call) to avoid a redundant provider call
        Uni<Collection<Flag>> allFlags = entry.allFlags.get();
        if (allFlags != null) {
            return entry.flags.computeIfAbsent(feature,
                    k -> allFlags.map(flags -> flags.stream()
                            .filter(f -> f.feature().equals(k))
                            .findFirst()
                            .orElse(null))
                            .memoize().indefinitely());
        }
        return entry.flags.computeIfAbsent(feature, k -> loader.get().memoize().indefinitely());
    }

    @Override
    public Uni<Void> invalidate(String providerId) {
        cache.remove(providerId);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> invalidateAll() {
        cache.clear();
        return Uni.createFrom().voidItem();
    }

    // Atomically returns a valid (non-expired) entry, replacing an expired one if needed
    private CacheEntry getValidEntry(String providerId) {
        return cache.compute(providerId, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new CacheEntry(newExpiry(key));
            }
            return existing;
        });
    }

    private long newExpiry(String providerId) {
        FlagsCacheConfig.ProviderCacheConfig providerConfig = config.providers().get(providerId);
        Duration ttl = providerConfig != null ? providerConfig.ttl().orElse(config.ttl()) : config.ttl();
        return System.nanoTime() + ttl.toNanos();
    }

    static class CacheEntry {
        final ConcurrentMap<String, Uni<Flag>> flags = new ConcurrentHashMap<>();
        final AtomicReference<Uni<Collection<Flag>>> allFlags = new AtomicReference<>();
        private final long expiresAt;

        CacheEntry(long expiresAt) {
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.nanoTime() >= expiresAt;
        }
    }

}
