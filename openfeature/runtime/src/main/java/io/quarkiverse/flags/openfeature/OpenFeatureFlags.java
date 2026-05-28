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
     * Registers a boolean flag with default value {@code false}.
     *
     * @param feature
     * @return {@code true} if the flag was registered, {@code false} if it was already registered
     */
    default boolean register(String feature) {
        return register(feature, FlagType.BOOLEAN, "false");
    }

    /**
     * Registers a flag with the given type and default value.
     *
     * @param feature
     * @param type
     * @param defaultValue the default value returned by OpenFeature if the flag cannot be resolved
     * @return {@code true} if the flag was registered, {@code false} if it was already registered
     */
    boolean register(String feature, FlagType type, String defaultValue);

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

    enum FlagType {

        BOOLEAN,
        STRING,
        INT;

        static FlagType fromString(String type) {
            return switch (type) {
                case "boolean" -> BOOLEAN;
                case "string" -> STRING;
                case "int" -> INT;
                default -> throw new IllegalArgumentException(
                        "Unsupported flag type: " + type + "; supported values: boolean, string, int");
            };
        }
    }

}
