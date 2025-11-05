package io.quarkiverse.flags.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.flags.buildtime")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface FlagsBuildTimeConfig {

    /**
     * Flag configurations.
     */
    @ConfigDocMapKey("flag-name")
    @WithParentName
    @WithDefaults
    Map<String, FlagConfig> flags();

}
