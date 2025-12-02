package io.quarkiverse.flags.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkiverse.flags.spi.FlagProvider;
import io.smallrye.mutiny.Uni;

@Singleton
public class ConfigFlagProvider extends AbstractFlagProvider {

    public static final int PRIORITY = FlagProvider.DEFAULT_PRIORITY + 1;

    private final FlagsBuildTimeConfig buildConfig;

    private final FlagsRuntimeConfig runtimeConfig;

    public ConfigFlagProvider(FlagManager manager, FlagsBuildTimeConfig buildConfig, FlagsRuntimeConfig runtimeConfig) {
        super(manager);
        this.buildConfig = buildConfig;
        this.runtimeConfig = runtimeConfig;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Uni<Collection<Flag>> getFlags() {
        List<Flag> ret = new ArrayList<>();
        addFlags(ret, buildConfig.flags());
        addFlags(ret, runtimeConfig.flags());
        return Uni.createFrom().item(List.copyOf(ret));
    }

    private void addFlags(List<Flag> ret, Map<String, FlagConfig> flags) {
        for (Entry<String, FlagConfig> entry : flags.entrySet()) {
            String feature = entry.getKey();
            Map<String, String> metadata = entry.getValue().meta();
            ret.add(Flag.builder(feature)
                    .setMetadata(metadata)
                    .setString(entry.getValue().value())
                    .build());
        }
    }

}
