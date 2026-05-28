package io.quarkiverse.flags.openfeature;

/**
 * A registry of feature flags resolved via OpenFeature.
 * <p>
 * Flags declared in configuration are registered automatically. Additional flags can be registered at runtime.
 * <p>
 * The container provides a CDI bean that implements this interface.
 */
public interface OpenFeatureFlags {

    /**
     * Registers a boolean flag.
     *
     * @param feature
     * @param defaultValue the default value returned by OpenFeature if the flag cannot be resolved
     * @return {@code true} if the flag was registered, {@code false} if it was already registered
     */
    boolean register(String feature, boolean defaultValue);

    /**
     * Registers a string flag.
     *
     * @param feature
     * @param defaultValue the default value returned by OpenFeature if the flag cannot be resolved
     * @return {@code true} if the flag was registered, {@code false} if it was already registered
     */
    boolean register(String feature, String defaultValue);

    /**
     * Registers an integer flag.
     *
     * @param feature
     * @param defaultValue the default value returned by OpenFeature if the flag cannot be resolved
     * @return {@code true} if the flag was registered, {@code false} if it was already registered
     */
    boolean register(String feature, int defaultValue);

    /**
     * Registers a double flag.
     *
     * @param feature
     * @param defaultValue the default value returned by OpenFeature if the flag cannot be resolved
     * @return {@code true} if the flag was registered, {@code false} if it was already registered
     */
    boolean register(String feature, double defaultValue);

    /**
     * Unregisters a flag.
     *
     * @param feature
     * @return {@code true} if the flag was unregistered, {@code false} if it was not registered
     */
    boolean unregister(String feature);

    /**
     * @param feature
     * @return {@code true} if the flag is registered
     */
    boolean isRegistered(String feature);

}
