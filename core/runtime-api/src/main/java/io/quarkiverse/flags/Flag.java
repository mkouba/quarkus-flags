package io.quarkiverse.flags;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import io.quarkiverse.flags.spi.ComputationContextImpl;
import io.quarkiverse.flags.spi.FlagBuilderImpl;
import io.quarkiverse.flags.spi.FlagManager;
import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A feature flag refers to a specific feature and provides several convenient methods to compute the current {@link Value}.
 * <p>
 * Use {@link Flags} as the central entry point for accessing feature flags.
 * Use {@link #builder(String)} to create a new flag programmatically.
 *
 * @see Flags
 * @see Builder
 */
public interface Flag {

    /**
     * @param feature the unique feature name; must not be {@code null} or blank
     * @return a new flag builder
     */
    static Builder builder(String feature) {
        return new FlagBuilderImpl(feature);
    }

    /**
     * There can be only one flag for a given feature at a given time.
     *
     * @return the name of the feature, never {@code null}
     */
    String feature();

    /**
     * The origin should identify the provider of the flag.
     *
     * @return the description of the source, never {@code null}
     */
    String origin();

    /**
     * Some keys have special meaning, e.g. {@value io.quarkiverse.flags.spi.FlagEvaluator#META_KEY} references
     * a {@link io.quarkiverse.flags.spi.FlagEvaluator}.
     *
     * @return the metadata, never {@code null}
     */
    default Map<String, String> metadata() {
        return Map.of();
    }

    /**
     * Computes the current value of the feature flag.
     * <p>
     * Does not block the caller thread. If the flag references a {@link io.quarkiverse.flags.spi.FlagEvaluator} in its
     * metadata, the evaluator is used to compute the value.
     *
     * @param context the computation context, must not be {@code null}
     * @return the computed value
     */
    @CheckReturnValue
    Uni<Value> compute(ComputationContext context);

    /**
     * Computes the current value of the feature flag with an empty {@link ComputationContext}.
     * <p>
     * Does not block the caller thread.
     *
     * @return the computed value
     * @see #compute(ComputationContext)
     */
    @CheckReturnValue
    default Uni<Value> compute() {
        return compute(ComputationContext.EMPTY);
    }

    /**
     * Computes the current value of the feature flag with an empty {@link ComputationContext}.
     * <p>
     * Blocks the caller thread.
     *
     * @return the computed value
     * @see #computeAndAwait(ComputationContext)
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
     * Computes the current value with an empty {@link ComputationContext} and returns its boolean representation.
     * <p>
     * Blocks the caller thread. Use {@link #computeAndAwait(ComputationContext)} when context is needed.
     *
     * @return the computed boolean value
     */
    default boolean isEnabled() {
        return computeAndAwait().asBoolean();
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its boolean representation, or the
     * default value if the value cannot be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param defaultValue the value to return if the value cannot be converted
     * @return the computed boolean value or the default
     */
    default boolean isEnabled(boolean defaultValue) {
        try {
            return computeAndAwait().asBoolean(defaultValue);
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its string representation.
     * <p>
     * Blocks the caller thread. Use {@link #computeAndAwait(ComputationContext)} when context is needed.
     *
     * @return the computed string value
     */
    default String getString() {
        return computeAndAwait().asString();
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its string representation, or the
     * default value if the value cannot be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param defaultValue the value to return if the value cannot be converted
     * @return the computed string value or the default
     */
    default String getString(String defaultValue) {
        try {
            return computeAndAwait().asString(defaultValue);
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its integer representation.
     * <p>
     * Blocks the caller thread. Use {@link #computeAndAwait(ComputationContext)} when context is needed.
     *
     * @return the computed integer value
     */
    default int getInt() {
        return computeAndAwait().asInt();
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its integer representation, or the
     * default value if the value cannot be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param defaultValue the value to return if the value cannot be converted
     * @return the computed integer value or the default
     */
    default int getInt(int defaultValue) {
        try {
            return computeAndAwait().asInt(defaultValue);
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its decimal representation.
     * <p>
     * Blocks the caller thread. Use {@link #computeAndAwait(ComputationContext)} when context is needed.
     *
     * @return the computed decimal value
     */
    default BigDecimal getDecimal() {
        return computeAndAwait().asDecimal();
    }

    /**
     * Computes the current value with an empty {@link ComputationContext} and returns its decimal representation, or the
     * default value if the value cannot be converted.
     * <p>
     * Blocks the caller thread.
     *
     * @param defaultValue the value to return if the value cannot be converted
     * @return the computed decimal value or the default
     */
    default BigDecimal getDecimal(BigDecimal defaultValue) {
        try {
            return computeAndAwait().asDecimal(defaultValue);
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * An immutable computed value of a feature flag. Provides conversion methods to obtain the value as boolean, string,
     * integer or decimal. A conversion may throw {@link NoSuchElementException} if the underlying type cannot be converted.
     *
     * @see BooleanValue
     * @see StringValue
     * @see IntValue
     * @see BigDecimalValue
     */
    interface Value {

        /**
         *
         * @return the boolean value
         * @throws NoSuchElementException if the value cannot be represented as boolean
         */
        boolean asBoolean();

        /**
         * @param defaultValue the value to return if conversion fails
         * @return the boolean value or the default
         */
        default boolean asBoolean(boolean defaultValue) {
            try {
                return asBoolean();
            } catch (NoSuchElementException e) {
                return defaultValue;
            }
        }

        /**
         *
         * @return the string value
         * @throws NoSuchElementException if the value cannot be represented as string
         */
        String asString();

        /**
         * @param defaultValue the value to return if conversion fails
         * @return the string value or the default
         */
        default String asString(String defaultValue) {
            try {
                return asString();
            } catch (NoSuchElementException e) {
                return defaultValue;
            }
        }

        /**
         *
         * @return the integer value
         * @throws NoSuchElementException if the value cannot be represented as integer
         */
        int asInt();

        /**
         * @param defaultValue the value to return if conversion fails
         * @return the integer value or the default
         */
        default int asInt(int defaultValue) {
            try {
                return asInt();
            } catch (NoSuchElementException e) {
                return defaultValue;
            }
        }

        /**
         *
         * @return the decimal value
         * @throws NoSuchElementException if the value cannot be represented as decimal
         */
        BigDecimal asDecimal();

        /**
         * @param defaultValue the value to return if conversion fails
         * @return the decimal value or the default
         */
        default BigDecimal asDecimal(BigDecimal defaultValue) {
            try {
                return asDecimal();
            } catch (NoSuchElementException e) {
                return defaultValue;
            }
        }
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
        Object get(String key);

        /**
         * @return an unmodifiable map of all context entries
         */
        default Map<String, Object> asMap() {
            return Collections.emptyMap();
        }

        interface Builder {

            Builder put(String key, Object value);

            ComputationContext build();

        }

    }

    /**
     * A convenient flag builder. Not reusable — a builder instance must not be used after {@link #build()} is called.
     * <p>
     * The value setters ({@link #setEnabled(boolean)}, {@link #setString(String)}, {@link #setInt(int)},
     * {@link #setDecimal(BigDecimal)}) are mutually exclusive — the last one called wins. Alternatively, use
     * {@link #setCompute(Function)} or {@link #setComputeAsync(Function)} to provide a dynamic evaluation function.
     * If neither a value nor a computing function is set, {@link BooleanValue#TRUE} is used as the default.
     */
    interface Builder {

        /**
         * Sets a fixed boolean value.
         *
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setEnabled(boolean value);

        /**
         * Sets a fixed string value.
         *
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setString(String value);

        /**
         * Sets a fixed integer value.
         *
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setInt(int value);

        /**
         * Sets a fixed decimal value.
         *
         * @param value
         * @return self
         * @see Flag#compute()
         */
        Builder setDecimal(BigDecimal value);

        /**
         * Sets a synchronous evaluation function that computes the value dynamically.
         *
         * @param fun
         * @return self
         * @see Flag#compute()
         * @see #setComputeAsync(Function)
         */
        default Builder setCompute(Function<ComputationContext, Value> fun) {
            if (fun == null) {
                throw new IllegalArgumentException("Compute function must not be null");
            }
            return setComputeAsync(cc -> Uni.createFrom().item(fun.apply(cc)));
        }

        /**
         * Sets an asynchronous evaluation function that computes the value dynamically.
         *
         * @param fun
         * @return self
         * @see Flag#compute()
         * @see #setCompute(Function)
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
         * By default, a flag can reference one evaluator in its metadata with a key
         * {@link io.quarkiverse.flags.spi.FlagEvaluator#META_KEY}. This evaluator is automatically used to compute the current
         * value for the flag produced by {@link #build()}.
         * <p>
         * {@link FlagManager#getEvaluator(String)} is used to obtain the evaluator instance. You can specify the manager
         * instance explicitly, otherwise the CDI lookup is performed.
         *
         * @param manager
         * @return self
         */
        Builder setFlagManager(FlagManager manager);

        /**
         * If neither value nor computing function is set then {@link BooleanValue#TRUE} is used.
         *
         * @return a new flag
         * @throws IllegalStateException if the origin is not set
         */
        Flag build();

    }

}
