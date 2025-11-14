package io.quarkiverse.flags;

import io.quarkiverse.flags.spi.FlagProvider;

/**
 * An in-memory feature flag provider.
 */
public interface InMemoryFlagProvider extends FlagProvider {

    static int PRIORITY = FlagProvider.DEFAULT_PRIORITY + 5;

    @Override
    default int getPriority() {
        return PRIORITY;
    }

    /**
     * @return {@code true} if the flag was added sucessfully, {@code false} otherwise
     * @see FlagAdded
     * @see Flag#builder(String)
     */
    boolean addFlag(Flag flag);

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
