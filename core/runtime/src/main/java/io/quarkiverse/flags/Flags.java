package io.quarkiverse.flags;

import java.util.List;
import java.util.Optional;

/**
 * Represents a central point to access feature flags.
 *
 * @see Flag
 */
public interface Flags {

    /**
     * @param feature
     * @return the flag for the given feature
     */
    Optional<Flag> find(String feature);

    /**
     * @return an immutable list of feature flags
     */
    List<Flag> findAll();

}
