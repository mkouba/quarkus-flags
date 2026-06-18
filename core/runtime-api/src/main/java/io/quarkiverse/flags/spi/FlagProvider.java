package io.quarkiverse.flags.spi;

import java.util.Collection;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flags;
import io.quarkus.runtime.BlockingOperationControl;
import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A provider of feature flags.
 * <p>
 * Implementation classes must be CDI beans. {@link jakarta.enterprise.context.Dependent} beans are reused.
 * <p>
 * Each provider must be annotated with {@link io.smallrye.common.annotation.Identifier} to define a unique identifier. If
 * multiple flag providers with the same identifier exist then the application fails to start.
 * <p>
 * The ordering of providers can be defined with {@link ComponentOrder}. A flag from a provider with higher priority takes
 * precedence and overrides flags with the same {@link Flag#feature()} from providers with lower priority.
 */
public interface FlagProvider {

    /**
     * Must not block the caller thread unless blocking is allowed.
     * <p>
     * An implementation can use {@link BlockingOperationControl#isBlockingAllowed()} to detect if blocking is allowed on the
     * current thread. If blocking is not allowed but an implementation still needs to perform a blocking operation then it has
     * to offload the execution on a worker thread.
     * <p>
     * The result must not contain flags with duplicate feature names.
     *
     * @return the flags
     * @see Flags#find(String)
     * @see Flags#findAll()
     * @see Flag#builder(String)
     */
    @CheckReturnValue
    Uni<Collection<Flag>> getFlags();

    /**
     * Must not block the caller thread unless blocking is allowed.
     * <p>
     * An implementation can use {@link BlockingOperationControl#isBlockingAllowed()} to detect if blocking is allowed on the
     * current thread. If blocking is not allowed but an implementation still needs to perform a blocking operation then it has
     * to offload the execution on a worker thread.
     * <p>
     * By default, this method filters the result of {@link #getFlags()}. Implementations backed by a database or remote service
     * should override this method with an optimized lookup.
     *
     * @param feature
     * @return the flag or {@code null}
     * @see Flags#find(String)
     */
    @CheckReturnValue
    default Uni<Flag> getFlag(String feature) {
        return getFlags().map(flags -> flags.stream()
                .filter(f -> f.feature().equals(feature))
                .findFirst()
                .orElse(null));
    }

    /**
     * Indicates whether the results of this provider can be cached.
     * <p>
     * Providers that manage flags in-memory (such as {@link io.quarkiverse.flags.InMemoryFlagProvider}) should return
     * {@code false} because caching would only introduce staleness with no performance benefit.
     * <p>
     * This value can be overridden per provider via {@code quarkus.flags.cache."provider-id".enabled}.
     *
     * @return {@code true} if caching is beneficial for this provider, {@code false} otherwise
     */
    default boolean isCacheable() {
        return true;
    }

}
