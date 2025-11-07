package io.quarkiverse.flags;

/**
 * A CDI event that is fired synchronously when a feature flag is removed from the system.
 */
public record FlagRemoved(Flag flag) {
}