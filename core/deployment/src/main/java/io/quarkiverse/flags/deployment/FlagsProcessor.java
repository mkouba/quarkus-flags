package io.quarkiverse.flags.deployment;

import io.quarkiverse.flags.CompositeFlagEvaluator;
import io.quarkiverse.flags.TimeSpanFlagEvaluator;
import io.quarkiverse.flags.runtime.ConfigFlagProvider;
import io.quarkiverse.flags.runtime.FlagManagerImpl;
import io.quarkiverse.flags.runtime.InMemoryFlagProviderImpl;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

public class FlagsProcessor {

    @BuildStep
    void beans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(FlagManagerImpl.class, ConfigFlagProvider.class,
                        InMemoryFlagProviderImpl.class, TimeSpanFlagEvaluator.class, CompositeFlagEvaluator.class)
                .build());
    }

}
