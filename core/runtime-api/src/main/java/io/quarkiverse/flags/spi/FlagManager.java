package io.quarkiverse.flags.spi;

import java.util.Optional;

import io.quarkiverse.flags.Flags;

/**
 * A feature flag manager.
 */
public interface FlagManager extends Flags {

    /**
     * @param id
     * @return the evaluator for the given id
     */
    Optional<FlagEvaluator> getEvaluator(String id);

}
