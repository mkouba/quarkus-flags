package io.quarkiverse.flags.security;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.logging.Logger;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.ImmutableBooleanValue;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag based on the current security identity.
 * <p>
 * If the initial value is {@code true} and the security identity is not anonymous (in case of {@value #AUTHENTICATED} metadata
 * is present) and the identity has an allowed role (in case of {@value #ROLES_ALLOWED} metadata is present). Otherwise, it
 * evaluates to {@code false}.
 */
@Singleton
public class SecurityIdentityFlagEvaluator implements FlagEvaluator {

    public static final String ID = "quarkus.security.identity";
    public static final String ROLES_ALLOWED = "roles-allowed";
    public static final String AUTHENTICATED = "authenticated";

    private static final Logger LOG = Logger.getLogger(SecurityIdentityFlagEvaluator.class);

    @Inject
    SecurityIdentity identity;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        if (initialValue.asBoolean()) {
            String authenticated = flag.metadata().get(AUTHENTICATED);
            if (authenticated != null
                    && Boolean.parseBoolean(authenticated)
                    && identity.isAnonymous()) {
                LOG.debugf("User not authenticated");
                return Uni.createFrom().item(ImmutableBooleanValue.FALSE);
            }
            String rolesAllowed = flag.metadata().get(ROLES_ALLOWED);
            if (rolesAllowed != null) {
                String[] roles = rolesAllowed.toString().split(",");
                for (String role : roles) {
                    if (identity.hasRole(role)) {
                        return Uni.createFrom().item(ImmutableBooleanValue.TRUE);
                    }
                }
                LOG.debugf("User [%s] has none of the allowed roles: %s", identity.getPrincipal().getName(), rolesAllowed);
                return Uni.createFrom().item(ImmutableBooleanValue.FALSE);
            }
        }
        return Uni.createFrom().item(initialValue);
    }

}
