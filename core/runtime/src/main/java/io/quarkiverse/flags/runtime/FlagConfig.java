package io.quarkiverse.flags.runtime;

import java.util.Map;

import io.smallrye.config.WithDefault;

/**
 * Configuration of a feature flag.
 */
public interface FlagConfig {

    /**
     * The value.
     */
    @WithDefault("true")
    String value();

    /**
     * The metadata.
     */
    Map<String, String> meta();

}