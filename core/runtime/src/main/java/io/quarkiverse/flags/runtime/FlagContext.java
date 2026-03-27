package io.quarkiverse.flags.runtime;

import java.util.List;

public class FlagContext {

    private final List<String> orderedProviderIds;

    public FlagContext(List<String> orderedProviderIds) {
        this.orderedProviderIds = orderedProviderIds;
    }

    public List<String> getOrderedProviderIds() {
        return orderedProviderIds;
    }

}
