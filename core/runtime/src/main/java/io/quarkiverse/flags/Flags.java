package io.quarkiverse.flags;

import java.util.List;
import java.util.NoSuchElementException;
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

    /**
     *
     * @param feature
     * @return the computed boolean value
     * @throws NoSuchElementException If no such feature exists
     */
    default boolean isEnabled(String feature) {
        return find(feature).orElseThrow().isEnabled();
    }

}
