package io.quarkiverse.flags.spi;

public abstract class AbstractFlagProvider implements FlagProvider {

    protected final FlagManager manager;

    public AbstractFlagProvider(FlagManager manager) {
        this.manager = manager;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

}
