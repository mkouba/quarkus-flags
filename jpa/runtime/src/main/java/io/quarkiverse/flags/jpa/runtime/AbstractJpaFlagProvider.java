package io.quarkiverse.flags.jpa.runtime;

import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkiverse.flags.spi.ImmutableStringValue;

public abstract class AbstractJpaFlagProvider extends AbstractFlagProvider {

    public AbstractJpaFlagProvider(FlagManager manager) {
        super(manager);
    }

    protected Flag createFlag(String feature, String value, Map<String, String> metadata) {
        return createFlag(feature, new ImmutableStringValue(value), metadata);
    }

}
