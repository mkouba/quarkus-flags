package io.quarkiverse.flags.cache.deployment;

import io.quarkiverse.flags.cache.CacheFlagCache;
import io.quarkiverse.flags.deployment.FlagProviderInfoBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.cache.deployment.spi.AdditionalCacheNameBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

public class FlagCacheProcessor {

    @BuildStep
    void beans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(new AdditionalBeanBuildItem(CacheFlagCache.class));
    }

    @BuildStep
    void registerCacheNames(FlagProviderInfoBuildItem providerInfo,
            BuildProducer<AdditionalCacheNameBuildItem> cacheNames) {
        for (String providerId : providerInfo.getOrderedProviderIds()) {
            cacheNames.produce(new AdditionalCacheNameBuildItem(CacheFlagCache.cacheName(providerId)));
        }
    }

}
