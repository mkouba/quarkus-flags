package io.quarkiverse.flags.openfeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Singleton;

import org.jboss.logging.Logger;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.Value;
import io.quarkiverse.flags.BigDecimalValue;
import io.quarkiverse.flags.BooleanValue;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkiverse.flags.IntValue;
import io.quarkiverse.flags.StringValue;
import io.quarkiverse.flags.openfeature.OpenFeatureFlags.FlagType;
import io.quarkiverse.flags.runtime.impl.ConfigFlagProvider;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.ComponentOrder;
import io.quarkiverse.flags.spi.FlagManager;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;

@Identifier(OpenFeatureFlagProvider.ID)
@ComponentOrder(after = InMemoryFlagProvider.ID, before = ConfigFlagProvider.ID)
@Singleton
public class OpenFeatureFlagProvider extends AbstractFlagProvider implements OpenFeatureFlags {

    public static final String ID = "quarkus.openfeature";

    private static final Logger LOG = Logger.getLogger(OpenFeatureFlagProvider.class);

    private final ConcurrentMap<String, FlagRegistration> registrations = new ConcurrentHashMap<>();

    private volatile Client client;

    OpenFeatureFlagProvider(FlagManager manager, OpenFeatureConfig config) {
        super(manager);
        for (Entry<String, OpenFeatureFlagConfig> entry : config.flags().entrySet()) {
            registrations.put(entry.getKey(),
                    new FlagRegistration(FlagType.fromString(entry.getValue().type()), entry.getValue().defaultValue()));
        }
    }

    // We need a different bean for "@Inject OpenFeatureFlags" because of the @Identifier qualifier
    @Typed(OpenFeatureFlags.class)
    @Produces
    public OpenFeatureFlags withDefaultQualifier() {
        return this;
    }

    @Override
    public boolean register(String feature, FlagType type, String defaultValue) {
        return registrations.putIfAbsent(feature, new FlagRegistration(type, defaultValue)) == null;
    }

    @Override
    public boolean unregister(String feature) {
        return registrations.remove(feature) != null;
    }

    @Override
    public boolean isRegistered(String feature) {
        return registrations.containsKey(feature);
    }

    @Override
    public Uni<Collection<Flag>> getFlags() {
        List<Flag> flags = new ArrayList<>();
        for (Entry<String, FlagRegistration> entry : registrations.entrySet()) {
            flags.add(buildFlag(entry.getKey(), entry.getValue()));
        }
        return Uni.createFrom().item(List.copyOf(flags));
    }

    @Override
    public Uni<Flag> getFlag(String feature) {
        FlagRegistration registration = registrations.get(feature);
        if (registration == null) {
            return Uni.createFrom().nullItem();
        }
        return Uni.createFrom().item(buildFlag(feature, registration));
    }

    private Flag buildFlag(String feature, FlagRegistration registration) {
        return Flag.builder(feature)
                .setOrigin(ID)
                .setComputeAsync(ctx -> Uni.createFrom().item(() -> evaluate(feature, registration, ctx)))
                .setFeatureManager(manager)
                .build();
    }

    private Client client() {
        Client c = client;
        if (c == null) {
            synchronized (this) {
                c = client;
                if (c == null) {
                    c = OpenFeatureAPI.getInstance().getClient();
                    client = c;
                }
            }
        }
        return c;
    }

    private Flag.Value evaluate(String feature, FlagRegistration registration,
            Flag.ComputationContext computationContext) {
        Client client = client();
        dev.openfeature.sdk.EvaluationContext evalCtx = mapContext(computationContext);
        return switch (registration.type()) {
            case BOOLEAN -> {
                boolean defaultVal = Boolean.parseBoolean(registration.defaultValue());
                FlagEvaluationDetails<Boolean> details = client.getBooleanDetails(feature, defaultVal, evalCtx);
                logIfError(feature, details);
                yield BooleanValue.from(details.getValue());
            }
            case STRING -> {
                FlagEvaluationDetails<String> details = client.getStringDetails(feature, registration.defaultValue(),
                        evalCtx);
                logIfError(feature, details);
                yield new StringValue(details.getValue());
            }
            case INT -> {
                int defaultVal = Integer.parseInt(registration.defaultValue());
                FlagEvaluationDetails<Integer> details = client.getIntegerDetails(feature, defaultVal, evalCtx);
                logIfError(feature, details);
                yield new IntValue(details.getValue());
            }
            case DOUBLE -> {
                double defaultVal = Double.parseDouble(registration.defaultValue());
                FlagEvaluationDetails<Double> details = client.getDoubleDetails(feature, defaultVal, evalCtx);
                logIfError(feature, details);
                yield new BigDecimalValue(BigDecimal.valueOf(details.getValue()));
            }
        };
    }

    private dev.openfeature.sdk.EvaluationContext mapContext(Flag.ComputationContext computationContext) {
        if (computationContext == null || computationContext == Flag.ComputationContext.EMPTY) {
            return new ImmutableContext();
        }
        Map<String, Object> data = computationContext.asMap();
        if (data.isEmpty()) {
            return new ImmutableContext();
        }
        String targetingKey = null;
        Map<String, Value> attributes = new HashMap<>();
        for (Entry<String, Object> entry : data.entrySet()) {
            if ("targetingKey".equals(entry.getKey())) {
                targetingKey = entry.getValue() != null ? entry.getValue().toString() : null;
            } else if (entry.getValue() != null) {
                attributes.put(entry.getKey(), Value.objectToValue(entry.getValue()));
            }
        }
        if (targetingKey != null) {
            return new ImmutableContext(targetingKey, attributes);
        }
        return new ImmutableContext(attributes);
    }

    private void logIfError(String feature, FlagEvaluationDetails<?> details) {
        if (details.getErrorCode() != null) {
            LOG.warnf("OpenFeature evaluation error for flag '%s': %s [%s]",
                    feature, details.getErrorCode(), details.getErrorMessage());
        }
    }

    record FlagRegistration(FlagType type, String defaultValue) {
    }

}
