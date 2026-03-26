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
 * Implementation classes must be CDI beans. Qualifiers are ignored. {@link jakarta.enterprise.context.Dependent} beans are
 * reused.
 */
public interface FlagProvider {

    /**
     * Must not block the caller thread unless blocking is allowed.
     * <p>
     * An implementation can use {@link BlockingOperationControl#isBlockingAllowed()} to detect if blocking is allowed on the
     * current thread. If blocking is not allowed but an implementation still needs to perform a blocking operation then it has
     * to offload the execution on a worker thread.
     * <p>
     * The result must not contain flags with duplicate feature names. A flag from a provider with higher priority takes
     * precedence and overrides flags with the same {@link Flag#feature()} from providers with lower priority.
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
     * The priority is reflected when the system collects all flags from all providers.
     * <p>
     * If multiple providers with the same priority are detected then the application fails to start.
     *
     * @return the priority
     */
    int getPriority();

    /**
     * The identifier must be unique.
     * <p>
     * If multiple flag providers with the same identifier exist then the application fails to start.
     *
     *
     * @return the identifier
     */
    String getId();

}
