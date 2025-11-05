package io.quarkiverse.flags.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.flags.runtime")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface FlagsRuntimeConfig {

    /**
     * Flag configurations.
     */
    @ConfigDocMapKey("flag-name")
    @WithParentName
    @WithDefaults
    Map<String, FlagConfig> flags();

}
