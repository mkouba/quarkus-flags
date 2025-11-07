package io.quarkiverse.flags;

import java.util.Map;
import java.util.NoSuchElementException;

import io.quarkiverse.flags.spi.ComputationContextImpl;
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
     * @return the metadata
     */
    default Map<String, String> metadata() {
        return Map.of();
    }

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Does not block the caller thread.
     *
     * @param context (not {@code null})
     * @return the computed value
     */
    @CheckReturnValue
    Uni<Value> compute(ComputationContext context);

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Does not block the caller thread.
     *
     * @return the computed value
     */
    @CheckReturnValue
    default Uni<Value> compute() {
        return compute(ComputationContext.EMPTY);
    }

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed value
     */
    default Value computeAndAwait() {
        return computeAndAwait(ComputationContext.EMPTY);
    }

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Blocks the caller thread.
     *
     * @param context (not {@code null})
     * @return the computed value
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
     * Context of a single computation.
     */
    interface ComputationContext {

        static ComputationContext EMPTY = builder().build();

        static ComputationContext of(String key, Object value) {
            return builder().put(key, value).build();
        }

        static Builder builder() {
            return new ComputationContextImpl.BuilderImpl();
        }

        /**
         * @param key
         * @return the data or {@code null}
         */
        <T> T get(String key);

        interface Builder {

            Builder put(String key, Object value);

            ComputationContext build();

        }

    }

}
