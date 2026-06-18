package io.quarkiverse.flags.runtime.impl;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.flags.cache")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface FlagsCacheConfig {

    /**
     * If set to {@code true} the flag provider cache is enabled.
     */
    @WithDefault("false")
    boolean enabled();

    /**
     * The default time-to-live for cache entries.
     */
    @WithDefault("10m")
    Duration ttl();

    /**
     * Per-provider cache configuration overrides.
     * <p>
     * The key is the value of the {@code @Identifier} annotation of the flag provider.
     */
    @WithParentName
    @ConfigDocMapKey("provider-id")
    Map<String, ProviderCacheConfig> providers();

    interface ProviderCacheConfig {

        /**
         * If set to {@code false}, the cache is bypassed for the given provider and the loader is always invoked.
         * <p>
         * This property only applies when the global {@code quarkus.flags.cache.enabled} is set to {@code true}.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * The time-to-live for cache entries of this provider. If not set, the global {@code quarkus.flags.cache.ttl} is used.
         */
        Optional<Duration> ttl();

    }

}
