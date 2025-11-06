package io.quarkiverse.flags;

import java.util.NoSuchElementException;

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
     * Computes the current value of the feature flag.
     * <p>
     * Does not block the caller thread.
     *
     * @param context (not {@code null})
     * @return the computed state
     */
    @CheckReturnValue
    Uni<Value> compute(ComputationContext context);

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Does not block the caller thread.
     *
     * @return the computed state
     */
    @CheckReturnValue
    default Uni<Value> compute() {
        return compute(null);
    }

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed state
     */
    default Value computeAndAwait() {
        return computeAndAwait(null);
    }

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Blocks the caller thread.
     *
     * @param context (not {@code null})
     * @return the computed state
     */
    default Value computeAndAwait(ComputationContext context) {
        return compute(context).await().indefinitely();
    }

    /**
     * Represents the value of a feature flag.
     */
    interface Value {

        /**
         *
         * @return the boolean value
         * @throws NoSuchElementException if the value cannot be represented as boolean
         */
        boolean asBoolean();

        /**
         *
         * @return the string value
         * @throws NoSuchElementException if the value cannot be represented as string
         */
        String asString();

        /**
         *
         * @return the integer value
         * @throws NoSuchElementException if the value cannot be represented as integer
         */
        int asInt();
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
