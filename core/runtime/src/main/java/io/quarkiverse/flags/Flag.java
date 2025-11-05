package io.quarkiverse.flags;

import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A feature flag.
 * 
 * @see FlagManager
 */
public interface Flag {

    /**
     * @return the name of the feature (not {@code null})
     */
    String feature();

    /**
     * Computes the current state of the feature flag.
     *
     * @return {@code true} if the feature flag is {@code on}, {@code false} otherwise
     */
    default boolean isOn() {
        return computeAndAwait().isOn();
    }

    /**
     * Computes the current state of the feature flag.
     * <p>
     * Does not block the caller thread.
     *
     * @param context (not {@code null})
     * @return the computed state
     */
    @CheckReturnValue
    Uni<State> compute(ComputationContext context);

    /**
     * Computes the current state of the feature flag.
     * <p>
     * Does not block the caller thread.
     *
     * @return the computed state
     */
    @CheckReturnValue
    default Uni<State> compute() {
        return compute(null);
    }

    /**
     * Computes the current state of the feature flag.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed state
     */
    default State computeAndAwait() {
        return computeAndAwait(null);
    }

    /**
     * Computes the current state of the feature flag.
     * <p>
     * Blocks the caller thread.
     *
     * @param context (not {@code null})
     * @return the computed state
     */
    default State computeAndAwait(ComputationContext context) {
        return compute(context).await().indefinitely();
    }

    /**
     * Represents a state of a feature flag.
     */
    interface State {

        /**
         * @return {@code true} if the feature flag is {@code on}, {@code false} otherwise
         */
        boolean isOn();

    }

    /**
     * Context of a single state computation.
     */
    interface ComputationContext {

        /**
         * @param key
         * @param data
         * @return this
         */
        ComputationContext put(String key, Object obj);

        /**
         * @param key
         * @return the data or {@code null}
         */
        <T> T get(String key);

    }

}
