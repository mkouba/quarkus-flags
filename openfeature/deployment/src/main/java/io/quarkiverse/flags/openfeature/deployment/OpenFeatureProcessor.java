package io.quarkiverse.flags.openfeature.deployment;

import io.quarkiverse.flags.openfeature.OpenFeatureFlagProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

public class OpenFeatureProcessor {

    @BuildStep
    void beans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(new AdditionalBeanBuildItem(OpenFeatureFlagProvider.class));
    }

}
