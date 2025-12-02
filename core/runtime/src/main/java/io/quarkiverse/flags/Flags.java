package io.quarkiverse.flags;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A central point to access feature flags.
 * <p>
 * The container provides a CDI bean that implements this inteface.
 *
 * @see Flag
 */
public interface Flags {

    /**
     * Does not block the caller thread.
     *
     * @param feature
     * @return the flag for the given feature
     */
    @CheckReturnValue
    Uni<Optional<Flag>> find(String feature);

    /**
     * Blocks the caller thread.
     *
     * @param feature
     * @return the flag for the given feature
     */
    default Optional<Flag> findAndAwait(String feature) {
        return find(feature).await().indefinitely();
    }

    /**
     * Does not block the caller thread.
     *
     * @return an immutable list of feature flags
     */
    @CheckReturnValue
    Uni<List<Flag>> findAll();

    /**
     * Blocks the caller thread.
     *
     * @return an immutable list of feature flags
     */
    default List<Flag> findAllAndAwait() {
        return findAll().await().indefinitely();
    }

    /**
     * Blocks the caller thread.
     *
     * @param feature
     * @return the computed boolean value
     * @throws NoSuchElementException If no such feature flag exists
     */
    default boolean isEnabled(String feature) {
        return findAndAwait(feature).orElseThrow().isEnabled();
    }

    /**
     * Blocks the caller thread.
     *
     * @param feature
     * @return the computed string value
     * @throws NoSuchElementException If no such feature flag exists
     */
    default String getString(String feature) {
        return findAndAwait(feature).orElseThrow().getString();
    }

    /**
     * Blocks the caller thread.
     *
     * @param feature
     * @return the computed integer value
     * @throws NoSuchElementException If no such feature flag exists
     */
    default int getInt(String feature) {
        return findAndAwait(feature).orElseThrow().getInt();
    }
}
