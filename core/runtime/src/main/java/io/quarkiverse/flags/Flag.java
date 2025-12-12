package io.quarkiverse.flags;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import io.quarkiverse.flags.runtime.ComputationContextImpl;
import io.quarkiverse.flags.runtime.FlagBuilderImpl;
import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A feature flag.
 */
public interface Flag {

    /**
     * @param feature (not {@code null})
     * @return a new flag builder
     */
    static Builder builder(String feature) {
        return new FlagBuilderImpl(feature);
    }

    /**
     * There can be only one flag for a given feature at a given time.
     *
     * @return the name of the feature (not {@code null})
     */
    String feature();

    /**
     * The origin should identify the provider of the flag.
     *
     * @return the description of the source
     */
    String origin();

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
     * Computes the current value and returns its boolean representation.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed boolean value
     */
    default boolean isEnabled() {
        return computeAndAwait().asBoolean();
    }

    /**
     * Computes the current value and returns its string representation.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed string value
     */
    default String getString() {
        return computeAndAwait().asString();
    }

    /**
     * Computes the current value and returns its integer representation.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed integer value
     */
    default int getInt() {
        return computeAndAwait().asInt();
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

    /**
     * A convenient flag builder.
     */
    interface Builder {

        /**
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setEnabled(boolean value);

        /**
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setString(String value);

        /**
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setInt(int value);

        /**
         * @param fun
         * @return self
         * @see Flag#compute()
         */
        default Builder setCompute(Function<ComputationContext, Value> fun) {
            return setComputeAsync(cc -> Uni.createFrom().item(fun.apply(cc)));
        }

        /**
         * @param fun
         * @return self
         * @see Flag#compute()
         */
        Builder setComputeAsync(Function<ComputationContext, Uni<Value>> fun);

        /**
         * @param metadata
         * @return self
         * @see Flag#metadata()
         */
        Builder setMetadata(Map<String, String> metadata);

        /**
         * @param origin
         * @return self
         * @see Flag#origin()
         */
        Builder setOrigin(String origin);

        /**
         * If neither value nor computing function is set then {@link BooleanValue#TRUE} is used.
         *
         * @return a new flag
         */
        Flag build();

    }

}
