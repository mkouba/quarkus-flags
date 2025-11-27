package io.quarkiverse.flags.security;

import java.security.Principal;
import java.util.OptionalInt;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.RolloutFlagEvaluator;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * {@link RolloutFlagEvaluator} implementation where the numerical representation of the current user is based on the current
 * security identity and the {@link Flag#feature()}.
 * <p>
 * It can be used to implement gradual rollout by increasing the {@value RolloutFlagEvaluator#ROLLOUT_PERCENTAGE} value.
 */
@Singleton
public class UsernameRolloutFlagEvaluator extends RolloutFlagEvaluator {

    public static final String ID = "quarkus.security.username-rollout";

    @Inject
    SecurityIdentity identity;

    @Override
    public String id() {
        return ID;
    }

    @Override
    protected OptionalInt getHash(Flag flag) {
        if (!identity.isAnonymous()) {
            Principal principal = identity.getPrincipal();
            if (principal != null) {
                String username = principal.getName();
                if (username != null && !username.isBlank()) {
                    // Make sure a different distribution is used for different features
                    String str = username + flag.feature();
                    return OptionalInt.of(str.hashCode());
                }
            }
        }
        return OptionalInt.empty();
    }

}
