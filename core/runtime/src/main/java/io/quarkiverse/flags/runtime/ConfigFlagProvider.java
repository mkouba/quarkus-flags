package io.quarkiverse.flags.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkiverse.flags.spi.ImmutableFlag;

@Priority(FlagProvider.DEFAULT_PRIORITY + 1)
@Singleton
public class ConfigFlagProvider implements FlagProvider {

    @Inject
    FlagsBuildTimeConfig buildConfig;

    @Inject
    FlagsRuntimeConfig runtimeConfig;

    @Override
    public Iterable<Flag> getFlags() {
        List<Flag> ret = new ArrayList<>();
        for (Entry<String, FlagConfig> entry : buildConfig.flags().entrySet()) {
            ret.add(new ImmutableFlag(entry.getKey(), entry.getValue().enabled()));
        }
        for (Entry<String, FlagConfig> entry : runtimeConfig.flags().entrySet()) {
            ret.add(new ImmutableFlag(entry.getKey(), entry.getValue().enabled()));
        }
        return List.copyOf(ret);
    }

}
