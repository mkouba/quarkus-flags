package io.quarkiverse.flags.openfeature;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.flags.openfeature")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OpenFeatureConfig {

    /**
     * Flag configurations.
     */
    @ConfigDocMapKey("flag-name")
    @WithParentName
    Map<String, OpenFeatureFlagConfig> flags();

}
