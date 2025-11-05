package io.quarkiverse.flags.runtime;

import io.smallrye.config.WithDefault;

public interface FlagConfig {

    /**
     * If flag is on or off.
     */
    @WithDefault("true")
    boolean enabled();

}