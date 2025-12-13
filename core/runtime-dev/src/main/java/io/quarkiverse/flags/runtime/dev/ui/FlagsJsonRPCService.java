package io.quarkiverse.flags.runtime.dev.ui;

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.Flags;
import io.quarkiverse.flags.runtime.FlagManagerImpl;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkus.runtime.annotations.JsonRpcDescription;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class FlagsJsonRPCService {

    @Inject
    Flags flags;

    List<FlagProvider> providers;

    Collection<FlagEvaluator> evaluators;

    public FlagsJsonRPCService(FlagManagerImpl flagManager) {
        this.providers = flagManager.getProviders();
        this.evaluators = flagManager.getEvaluators();
    }

    @JsonRpcDescription("Get information about feature flags used in the application")
    public JsonArray getFlagsData() {
        JsonArray data = new JsonArray();
        for (Flag flag : flags.findAllAndAwait()) {
            JsonObject flagJson = new JsonObject();
            flagJson.put("feature", flag.feature());
            flagJson.put("origin", flag.origin());
            flagJson.put("metadata", flag.metadata().entrySet().stream()
                    .map(e -> new JsonObject().put("key", e.getKey()).put("value", e.getValue())));
            data.add(flagJson);
        }
        return data;
    }

    @JsonRpcDescription("Compute the value of a flag for the specific feature")
    public Uni<String> computeValue(String feature) {
        return flags.find(feature).chain(f -> f.orElseThrow().compute().map(Value::asString));
    }

    @JsonRpcDescription("Get information about flag providers")
    public JsonArray getFlagProvidersData() {
        JsonArray data = new JsonArray();
        for (FlagProvider p : providers) {
            JsonObject providerJson = new JsonObject();
            providerJson.put("id", p.getId());
            providerJson.put("priority", p.getPriority());
            data.add(providerJson);
        }
        return data;
    }

    @JsonRpcDescription("Get feature flags for a specific flag provider")
    public Uni<JsonArray> getProviderFlags(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid provider id: " + id);
        }
        FlagProvider provider = providers.stream().filter(e -> e.getId().equals(id)).findAny().orElseThrow();
        if (provider == null) {
            throw new IllegalArgumentException("Provider with given id does not exist: " + id);
        }
        return provider.getFlags().map(flags -> {
            JsonArray array = new JsonArray();
            for (Flag flag : flags) {
                array.add(flag.feature());
            }
            return array;
        });
    }

    @JsonRpcDescription("Get information about flag evaluators")
    public JsonArray getFlagEvaluatorsData() {
        JsonArray data = new JsonArray();
        for (FlagEvaluator e : evaluators) {
            JsonObject evaluatorJson = new JsonObject();
            evaluatorJson.put("id", e.getId());
            evaluatorJson.put("className", e.getClass().getName());
            data.add(evaluatorJson);
        }
        return data;
    }

}
