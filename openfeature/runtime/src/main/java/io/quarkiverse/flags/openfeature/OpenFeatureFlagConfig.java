package io.quarkiverse.flags.openfeature;

/**
 * Configuration of a feature flag resolved via OpenFeature.
 */
public interface OpenFeatureFlagConfig {

    /**
     * The type of the flag value.
     * <p>
     * Determines which OpenFeature evaluation method is used. Supported values: {@code boolean}, {@code string}, {@code int},
     * {@code double}.
     */
    String type();

    /**
     * The default value returned by OpenFeature if the flag cannot be resolved.
     */
    String defaultValue();

}
