package io.quarkiverse.flags.spi;

/**
 * Base class for {@link FlagProvider} implementations that need access to the {@link FlagManager}.
 */
public abstract class AbstractFlagProvider implements FlagProvider {

    protected final FlagManager manager;

    public AbstractFlagProvider(FlagManager manager) {
        this.manager = manager;
    }

}
