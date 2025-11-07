package io.quarkiverse.flags;

import java.util.Optional;
import java.util.Set;

import io.quarkiverse.flags.spi.FlagEvaluator;

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
     * Collects all flags from all providers.
     * <p>
     * A flag from a provider with higher priority takes precedence and overrides flags with the same {@link Flag#feature()}
     * from providers with lower priority.
     *
     * @return an immutable set of feature flags
     */
    Set<Flag> getFlags();

    /**
     * @param id
     * @return the evaluator for the given id
     */
    Optional<FlagEvaluator> getEvaluator(String id);

}
