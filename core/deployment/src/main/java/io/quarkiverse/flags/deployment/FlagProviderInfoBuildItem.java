package io.quarkiverse.flags.deployment;

import java.util.List;
import java.util.Map;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Holds the build-time validated and topologically sorted provider info.
 */
public final class FlagProviderInfoBuildItem extends SimpleBuildItem {

    private final List<String> orderedProviderIds;
    // provider id -> list of provider ids declared in @ComponentOrder#before()
    private final Map<String, List<String>> beforeEdges;
    // provider id -> list of provider ids declared in @ComponentOrder#after()
    private final Map<String, List<String>> afterEdges;

    public FlagProviderInfoBuildItem(List<String> orderedProviderIds,
            Map<String, List<String>> beforeEdges, Map<String, List<String>> afterEdges) {
        this.orderedProviderIds = orderedProviderIds;
        this.beforeEdges = beforeEdges;
        this.afterEdges = afterEdges;
    }

    public List<String> getOrderedProviderIds() {
        return orderedProviderIds;
    }

    public Map<String, List<String>> getBeforeEdges() {
        return beforeEdges;
    }

    public Map<String, List<String>> getAfterEdges() {
        return afterEdges;
    }

}
