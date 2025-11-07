package io.quarkiverse.flags;

/**
 * A CDI event that is fired synchronously when a new feature flag is added to the system.
 */
public record FlagAdded(Flag flag) {
}