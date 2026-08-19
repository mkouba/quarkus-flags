package io.quarkiverse.flags.spi;

import java.util.OptionalInt;

import io.quarkiverse.flags.BooleanValue;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag using a simple percentage-based rollout strategy, based on a consistent numerical representation of the
 * current user.
 * <p>
 * It can be used to implement gradual rollout by increasing the {@value RolloutFlagEvaluator#ROLLOUT_PERCENTAGE} metadata
 * value. The percentage must be a value between {@code 0} and {@code 100} (inclusive):
 * <ul>
 * <li>{@code 0} - the flag is disabled for all users,</li>
 * <li>{@code 100} - the flag is enabled for all users (including users without a stable hash, such as anonymous users),</li>
 * <li>{@code 1} - {@code 99} - the flag is enabled for the given percentage of users; a user without a stable hash (e.g. an
 * anonymous user) is never enabled.</li>
 * </ul>
 * The percentage is only taken into account when the boolean representation of the initial value is {@code true}; a flag whose
 * initial value is {@code false} (i.e. disabled by default) stays disabled for all users regardless of the percentage.
 * <p>
 * If the {@value RolloutFlagEvaluator#ROLLOUT_PERCENTAGE} metadata is not set then the initial value is returned as-is, i.e. an
 * enabled flag is enabled for all users.
 */
public abstract class RolloutFlagEvaluator implements FlagEvaluator {

    public static final String ROLLOUT_PERCENTAGE = "rollout-percentage";

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        if (initialValue != null && initialValue.asBoolean(false)) {
            String rolloutPercentage = flag.metadata().get(ROLLOUT_PERCENTAGE);
            if (rolloutPercentage != null) {
                int percentage;
                try {
                    percentage = Integer.parseInt(rolloutPercentage);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid rollout percentage value: " + rolloutPercentage);
                }
                if (percentage < 0 || percentage > 100) {
                    throw new IllegalStateException(
                            "Rollout percentage must be a value between 0 and 100 (inclusive): " + percentage);
                }
                // 0% is disabled for everyone; 100% is enabled for everyone
                // In both cases the user hash is irrelevant and the bucketing logic is skipped
                if (percentage == 0) {
                    return BooleanValue.createUni(false);
                }
                if (percentage == 100) {
                    return BooleanValue.createUni(true);
                }
                OptionalInt userHash = getHash(flag, computationContext);
                if (userHash.isEmpty()) {
                    return BooleanValue.createUni(false);
                }
                int bucket = Math.floorMod(userHash.getAsInt(), 100);
                return BooleanValue.createUni(bucket < percentage);
            }
        }
        return Uni.createFrom().item(initialValue);
    }

    /**
     * The returned hash is used to calculate the bucket.
     *
     * @param flag
     * @return the numerical representation of the current user
     */
    protected abstract OptionalInt getHash(Flag flag, ComputationContext computationContext);

}
