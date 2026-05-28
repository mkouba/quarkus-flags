package io.quarkiverse.flags.openfeature;

import io.smallrye.config.WithDefault;

/**
 * Configuration of a feature flag resolved via OpenFeature.
 */
public interface OpenFeatureFlagConfig {

    /**
     * The type of the flag value.
     * <p>
     * Determines which OpenFeature evaluation method is used. Supported values: {@code boolean}, {@code string}, {@code int}.
     */
    @WithDefault("boolean")
    String type();

    /**
     * The default value returned by OpenFeature if the flag cannot be resolved.
     */
    @WithDefault("true")
    String defaultValue();

}
