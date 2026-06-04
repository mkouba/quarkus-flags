package io.quarkiverse.flags;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A central point to access feature flags.
 * <p>
 * The container provides a CDI bean that implements this interface.
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
     * Returns the computed boolean value, or the default if the flag is not found, computation fails, or the value cannot
     * be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param feature
     * @param defaultValue the value to return on failure
     * @return the computed boolean value or the default
     */
    default boolean isEnabled(String feature, boolean defaultValue) {
        Optional<Flag> flag = findAndAwait(feature);
        return flag.isPresent() ? flag.get().isEnabled(defaultValue) : defaultValue;
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
     * Returns the computed string value, or the default if the flag is not found, computation fails, or the value cannot
     * be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param feature
     * @param defaultValue the value to return on failure
     * @return the computed string value or the default
     */
    default String getString(String feature, String defaultValue) {
        Optional<Flag> flag = findAndAwait(feature);
        return flag.isPresent() ? flag.get().getString(defaultValue) : defaultValue;
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

    /**
     * Returns the computed integer value, or the default if the flag is not found, computation fails, or the value cannot
     * be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param feature
     * @param defaultValue the value to return on failure
     * @return the computed integer value or the default
     */
    default int getInt(String feature, int defaultValue) {
        Optional<Flag> flag = findAndAwait(feature);
        return flag.isPresent() ? flag.get().getInt(defaultValue) : defaultValue;
    }

    /**
     * Blocks the caller thread.
     *
     * @param feature
     * @return the computed decimal value
     * @throws NoSuchElementException If no such feature flag exists
     */
    default BigDecimal getDecimal(String feature) {
        return findAndAwait(feature).orElseThrow().getDecimal();
    }

    /**
     * Returns the computed decimal value, or the default if the flag is not found, computation fails, or the value cannot
     * be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param feature
     * @param defaultValue the value to return on failure
     * @return the computed decimal value or the default
     */
    default BigDecimal getDecimal(String feature, BigDecimal defaultValue) {
        Optional<Flag> flag = findAndAwait(feature);
        return flag.isPresent() ? flag.get().getDecimal(defaultValue) : defaultValue;
    }
}
