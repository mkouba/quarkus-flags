package io.quarkiverse.flags.runtime.dev.ui;

import java.time.Duration;
import java.util.Map.Entry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.Flags;
import io.quarkiverse.flags.runtime.impl.FlagManagerImpl;
import io.quarkiverse.flags.runtime.impl.FlagManagerImpl.FlagProviderWithId;
import io.quarkiverse.flags.runtime.impl.FlagsCacheConfig;
import io.quarkiverse.flags.runtime.impl.FlagsCacheConfig.ProviderCacheConfig;
import io.quarkiverse.flags.spi.FlagCache;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkus.runtime.annotations.JsonRpcDescription;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class FlagsJsonRPCService {

    private final Flags flags;

    private final FlagManagerImpl flagManager;

    private final FlagsCacheConfig cacheConfig;

    // null if caching is not enabled or no FlagCache bean is available
    private final FlagCache cache;

    public FlagsJsonRPCService(Flags flags, FlagManagerImpl flagManager, FlagsCacheConfig cacheConfig,
            @Any Instance<FlagCache> cacheInstance) {
        this.flags = flags;
        this.cacheConfig = cacheConfig;
        this.flagManager = flagManager;
        this.cache = cacheInstance.isResolvable() ? cacheInstance.get() : null;
    }

    @JsonRpcDescription("Get information about feature flags used in the application")
    public JsonArray getFlagsData() {
        JsonArray data = new JsonArray();
        for (Flag flag : flags.findAllAndAwait()) {
            JsonObject flagJson = new JsonObject();
            flagJson.put("feature", flag.feature());
            flagJson.put("origin", flag.origin());
            JsonArray metadataArray = new JsonArray();
            for (Entry<String, String> e : flag.metadata().entrySet()) {
                metadataArray.add(new JsonObject().put("key", e.getKey()).put("value", e.getValue()));
            }
            flagJson.put("metadata", metadataArray);
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
        for (FlagProviderWithId p : flagManager.getProviders()) {
            JsonObject providerJson = new JsonObject();
            providerJson.put("id", p.id());
            data.add(providerJson);
        }
        return data;
    }

    @JsonRpcDescription("Get feature flags for a specific flag provider")
    public Uni<JsonArray> getProviderFlags(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid provider id: " + id);
        }
        FlagProvider provider = flagManager.getProviders().stream()
                .filter(p -> p.id().equals(id))
                .findAny()
                .orElseThrow()
                .provider();
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
        for (Entry<String, FlagEvaluator> e : flagManager.getEvaluators().entrySet()) {
            JsonObject evaluatorJson = new JsonObject();
            evaluatorJson.put("id", e.getKey());
            evaluatorJson.put("className", e.getValue().getClass().getName());
            data.add(evaluatorJson);
        }
        return data;
    }

    @JsonRpcDescription("Get information about the flag cache, its configuration and implementation")
    public JsonObject getCacheData() {
        JsonObject data = new JsonObject();
        boolean enabled = cacheConfig.enabled();
        data.put("enabled", enabled);
        data.put("defaultTtl", formatDuration(cacheConfig.ttl()));
        if (enabled) {
            data.put("implementationClass", cache.getClass().getName());
        }
        JsonArray providers = new JsonArray();
        for (FlagProviderWithId p : flagManager.getProviders()) {
            JsonObject providerJson = new JsonObject();
            providerJson.put("id", p.id());
            ProviderCacheConfig providerConfig = cacheConfig.providers().get(p.id());
            boolean cachingEnabled;
            if (enabled) {
                cachingEnabled = providerConfig != null ? providerConfig.enabled() : p.provider().isCacheable();
            } else {
                cachingEnabled = false;
            }
            providerJson.put("cachingEnabled", cachingEnabled);
            Duration ttl = providerConfig != null ? providerConfig.ttl().orElse(cacheConfig.ttl()) : cacheConfig.ttl();
            providerJson.put("ttl", formatDuration(ttl));
            providers.add(providerJson);
        }
        data.put("providers", providers);
        return data;
    }

    @JsonRpcDescription("Invalidate all flag cache entries")
    public Uni<Boolean> invalidateCache() {
        if (cache != null) {
            return cache.invalidateAll().replaceWith(true);
        }
        return Uni.createFrom().item(false);
    }

    @JsonRpcDescription("Invalidate the flag cache entry for a specific provider")
    public Uni<Boolean> invalidateProviderCache(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid provider id: " + id);
        }
        if (cache != null) {
            return cache.invalidate(id).replaceWith(true);
        }
        return Uni.createFrom().item(false);
    }

    /**
     * Formats a {@link Duration} into a compact human-readable form, e.g. {@code 10m} or {@code 1h 30m}.
     */
    static String formatDuration(Duration duration) {
        long days = duration.toDaysPart();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        int millis = duration.toMillisPart();
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s ");
        }
        if (millis > 0) {
            sb.append(millis).append("ms ");
        }
        return sb.isEmpty() ? "0s" : sb.toString().strip();
    }

}
