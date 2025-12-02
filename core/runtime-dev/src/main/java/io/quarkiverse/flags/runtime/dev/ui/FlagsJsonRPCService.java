package io.quarkiverse.flags.runtime.dev.ui;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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

    List<Entry<String, FlagProvider>> providers;

    Collection<FlagEvaluator> evaluators;

    public FlagsJsonRPCService(FlagManagerImpl flagManager) {
        this.providers = flagManager.getProviders().stream().map(p -> Map.entry(UUID.randomUUID().toString(), p)).toList();
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
        return flags.findAndAwait(feature).orElseThrow().compute().map(Value::asString);
    }

    @JsonRpcDescription("Get information about flag providers")
    public JsonArray getFlagProvidersData() {
        JsonArray data = new JsonArray();
        for (Entry<String, FlagProvider> e : providers) {
            JsonObject providerJson = new JsonObject();
            providerJson.put("id", e.getKey());
            providerJson.put("className", e.getValue().getClass().getName());
            providerJson.put("priority", e.getValue().getPriority());
            data.add(providerJson);
        }
        return data;
    }

    @JsonRpcDescription("Get feature flags for a specific flag provider")
    public Uni<JsonArray> getProviderFlags(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid provider id");
        }
        FlagProvider provider = providers.stream().filter(e -> e.getKey().equals(id)).findAny().orElseThrow().getValue();
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
            evaluatorJson.put("id", e.id());
            evaluatorJson.put("className", e.getClass().getName());
            data.add(evaluatorJson);
        }
        return data;
    }

}
