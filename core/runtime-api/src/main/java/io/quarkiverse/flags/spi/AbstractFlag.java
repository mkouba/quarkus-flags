package io.quarkiverse.flags.spi;

import java.util.Map;
import java.util.Objects;

import io.quarkiverse.flags.Flag;

public abstract class AbstractFlag implements Flag {

    private final String feature;

    private final String origin;

    private final Map<String, String> metadata;

    protected AbstractFlag(String feature, String origin, Map<String, String> metadata) {
        this.feature = Objects.requireNonNull(feature);
        this.origin = Objects.requireNonNull(origin);
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
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
