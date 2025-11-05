package io.quarkiverse.flags;

import java.util.Optional;
import java.util.Set;

/**
 * A feature flag manager.
 */
public interface FlagManager {

    /**
     * @param feature
     * @return the flag for the specific feature
     */
    Optional<Flag> getFlag(String feature);

    /**
     *
     * @return an immutable set of feature flags
     */
    Set<Flag> getFlags();

}
