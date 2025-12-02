package io.quarkiverse.flags.spi;

import java.util.Collection;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flags;
import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Uni;

/**
 * A provider of feature flags.
 * <p>
 * Implementation classes must be CDI beans. Qualifiers are ignored. {@link jakarta.enterprise.context.Dependent} beans are
 * reused.
 */
public interface FlagProvider {

    int DEFAULT_PRIORITY = 1;

    /**
     * Must not block the caller thread. If an implementation needs to perform a blocking operation then it has to offload the
     * execution on a worker thread.
     * <p>
     * The result must not contain flags with duplicate feature names.
     * <p>
     * A flag from a provider with higher priority takes precedence and overrides flags with the same {@link Flag#feature()}
     * from providers with lower priority.
     *
     * @return the flags
     * @see Flags#find(String)
     * @see Flags#findAll()
     * @see Flag#builder(String)
     */
    @CheckReturnValue
    Uni<Collection<Flag>> getFlags();

    /**
     * The priority is reflected when the system collects all flags from all providers.
     *
     * @return the priority
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

}
