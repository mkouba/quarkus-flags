package io.quarkiverse.flags.spi;

import java.util.OptionalInt;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag using a simple percentage-based rollout strategy, based on a consistent numerical representation of the
 * current user.
 * <p>
 * It can be used to implement gradual rollout by increasing the {@value RolloutFlagEvaluator#ROLLOUT_PERCENTAGE} metadata
 * value.
 */
public abstract class RolloutFlagEvaluator implements FlagEvaluator {

    public static final String ROLLOUT_PERCENTAGE = "rollout-percentage";

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        if (initialValue.asBoolean()) {
            String rolloutPercentage = flag.metadata().get(ROLLOUT_PERCENTAGE);
            if (rolloutPercentage != null) {
                int percentage;
                try {
                    percentage = Integer.parseInt(rolloutPercentage);
                    if (percentage < 1 || percentage > 99) {
                        throw new IllegalStateException(
                                "Rollout percentage must be a value between 1 and 99 (inclusive): " + percentage);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid rollout percentage value: " + rolloutPercentage);
                }
                OptionalInt userHash = getHash(flag);
                if (userHash.isEmpty()) {
                    return Uni.createFrom().item(ImmutableBooleanValue.FALSE);
                }
                int bucket = Math.abs(userHash.getAsInt() % 100);
                return Uni.createFrom().item(ImmutableBooleanValue.from(bucket < percentage));
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
    protected abstract OptionalInt getHash(Flag flag);

}
