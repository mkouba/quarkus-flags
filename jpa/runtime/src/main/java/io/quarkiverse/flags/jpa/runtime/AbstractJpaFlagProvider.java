package io.quarkiverse.flags.jpa.runtime;

import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;

public abstract class AbstractJpaFlagProvider extends AbstractFlagProvider {

    public AbstractJpaFlagProvider(FlagManager manager) {
        super(manager);
    }

    protected Flag createFlag(String feature, String value, Map<String, String> metadata) {
        return Flag.builder(feature).setMetadata(metadata).setString(value).build();
    }

}
