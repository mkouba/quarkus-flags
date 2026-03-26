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
import io.smallrye.mutiny.Uni;

@Singleton
public class ConfigFlagProvider extends AbstractFlagProvider {

    public static final int PRIORITY = 200;

    private final List<Flag> buildConfigFlags;

    private final FlagsRuntimeConfig runtimeConfig;

    public ConfigFlagProvider(FlagManager manager, FlagsBuildTimeConfig buildConfig, FlagsRuntimeConfig runtimeConfig) {
        super(manager);
        // Build config flags are immutable
        List<Flag> buildConfigFlags = new ArrayList<>();
        addFlags(buildConfigFlags, buildConfig.flags());
        this.buildConfigFlags = buildConfigFlags;
        this.runtimeConfig = runtimeConfig;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Uni<Collection<Flag>> getFlags() {
        List<Flag> ret = new ArrayList<>();
        ret.addAll(buildConfigFlags);
        addFlags(ret, runtimeConfig.flags());
        return Uni.createFrom().item(List.copyOf(ret));
    }

    @Override
    public Uni<Flag> getFlag(String feature) {
        Flag found = null;
        for (Flag flag : buildConfigFlags) {
            if (flag.feature().equals(feature)) {
                found = flag;
                break;
            }
        }
        if (found == null) {
            found = findFlag(feature, runtimeConfig.flags());
        }
        return Uni.createFrom().item(found);
    }

    private void addFlags(List<Flag> flags, Map<String, FlagConfig> config) {
        for (Entry<String, FlagConfig> entry : config.entrySet()) {
            String feature = entry.getKey();
            Map<String, String> metadata = entry.getValue().meta();
            flags.add(Flag.builder(feature)
                    .setMetadata(metadata)
                    .setString(entry.getValue().value())
                    .setFeatureManager(manager)
                    .build());
        }
    }

    private Flag findFlag(String feature, Map<String, FlagConfig> config) {
        for (Entry<String, FlagConfig> entry : config.entrySet()) {
            if (feature.equals(entry.getKey())) {
                return Flag.builder(feature)
                        .setMetadata(entry.getValue().meta())
                        .setString(entry.getValue().value())
                        .setFeatureManager(manager)
                        .build();
            }
        }
        return null;
    }

}
