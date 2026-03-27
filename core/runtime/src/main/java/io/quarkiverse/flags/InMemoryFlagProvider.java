package io.quarkiverse.flags;

import io.quarkiverse.flags.spi.FlagProvider;

/**
 * An in-memory feature flag provider.
 * <p>
 * The container provides a CDI bean that implements this interface.
 */
public interface InMemoryFlagProvider extends FlagProvider {

    /**
     * Builds the flag and adds it to the provider. The {@link Flag#origin()} is set automatically.
     *
     * @param builder
     * @return {@code true} if the flag was added successfully, {@code false} otherwise
     * @see FlagAdded
     * @see Flag#builder(String)
     */
    boolean addFlag(Flag.Builder builder);

    /**
     * @param feature
     * @return the removed flag, or {@code null}
     * @see FlagRemoved
     */
    Flag removeFlag(String feature);

    /**
     * A CDI event that is fired synchronously when a new feature flag is added to the system.
     */
    record FlagAdded(Flag flag) {
    }

    /**
     * A CDI event that is fired synchronously when a feature flag is removed from the system.
     */
    record FlagRemoved(Flag flag) {
    }

}
