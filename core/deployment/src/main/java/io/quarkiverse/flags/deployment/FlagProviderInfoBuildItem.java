package io.quarkiverse.flags.deployment;

import java.util.List;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Holds the build-time validated and topologically sorted provider info.
 */
public final class FlagProviderInfoBuildItem extends SimpleBuildItem {

    private final List<String> orderedProviderIds;

    public FlagProviderInfoBuildItem(List<String> orderedProviderIds) {
        this.orderedProviderIds = orderedProviderIds;
    }

    public List<String> getOrderedProviderIds() {
        return orderedProviderIds;
    }

}
