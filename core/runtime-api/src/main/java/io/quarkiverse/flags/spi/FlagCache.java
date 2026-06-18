package io.quarkiverse.flags.spi;

import java.util.Collection;
import java.util.function.Supplier;

import io.quarkiverse.flags.Flag;
import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A cache for flag provider results.
 * <p>
 * Implementations must be CDI beans. The cache is used by the flag manager to avoid repeated invocations of
 * {@link FlagProvider#getFlags()} and {@link FlagProvider#getFlag(String)}.
 * <p>
 * There is one lazily populated cache entry per provider. {@link #getOrComputeFlags(String, Supplier)} caches all flags
 * from the provider at once. {@link #getOrComputeFlag(String, String, Supplier)} first checks for an individually cached
 * flag, then falls back to the all-flags entry if available, and only invokes the loader as a last resort.
 * <p>
 * Implementations should define a TTL (time-to-live) per provider. When the TTL expires, the entire provider entry
 * (including both all-flags and individual flag entries) should be discarded. The built-in implementation uses
 * {@code quarkus.flags.cache.ttl} as the default TTL and supports per-provider overrides via
 * {@code quarkus.flags.cache."provider-id".ttl}. Caching can be disabled for a specific provider via
 * {@code quarkus.flags.cache."provider-id".enabled=false} or by returning {@code false} from
 * {@link FlagProvider#isCacheable()}. The configuration property takes precedence over the provider's declaration.
 * <p>
 * CDI qualifiers declared on the bean class are ignored when the flag manager looks up the cache.
 *
 * @see FlagProvider
 */
public interface FlagCache {

    /**
     * Returns all cached flags for the given provider, or invokes the loader and caches the result.
     *
     * @param providerId the unique identifier of the flag provider
     * @param loader supplies the flags when no valid cache entry exists
     * @return the cached or freshly loaded flags
     */
    @CheckReturnValue
    Uni<Collection<Flag>> getOrComputeFlags(String providerId, Supplier<Uni<Collection<Flag>>> loader);

    /**
     * Returns the cached flag for the given provider and feature, or invokes the loader and caches the result.
     * <p>
     * Only the individual flag entry for the given feature is added or refreshed.
     *
     * @param providerId the unique identifier of the flag provider
     * @param feature the feature name
     * @param loader supplies the flag when no valid cache entry exists; may return {@code null}
     * @return the cached or freshly loaded flag, may be {@code null}
     */
    @CheckReturnValue
    Uni<Flag> getOrComputeFlag(String providerId, String feature, Supplier<Uni<Flag>> loader);

    /**
     * Invalidates the cache entry for the given provider.
     *
     * @param providerId the unique identifier of the flag provider
     * @return a {@link Uni} that completes when the entry is invalidated
     */
    @CheckReturnValue
    Uni<Void> invalidate(String providerId);

    /**
     * Invalidates all cache entries.
     *
     * @return a {@link Uni} that completes when all entries are invalidated
     */
    @CheckReturnValue
    Uni<Void> invalidateAll();

}
