package io.quarkiverse.flags.spi;

import java.util.Map;

import io.quarkiverse.flags.Flag;

public abstract class AbstractFlag implements Flag {

    private final String feature;

    private final String origin;

    private final Map<String, String> metadata;

    protected AbstractFlag(String feature, String origin, Map<String, String> metadata) {
        this.feature = feature;
        this.origin = origin;
        this.metadata = Map.copyOf(metadata);
    }

    @Override
    public String feature() {
        return feature;
    }

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

}
