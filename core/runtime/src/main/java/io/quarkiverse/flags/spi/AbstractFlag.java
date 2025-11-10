package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;

public abstract class AbstractFlag implements Flag {

    private final String feature;

    private final Map<String, String> metadata;

    protected AbstractFlag(String feature, Map<String, String> metadata) {
        this.feature = feature;
        this.metadata = Map.copyOf(metadata);
    }

    @Override
    public String feature() {
        return feature;
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

}
