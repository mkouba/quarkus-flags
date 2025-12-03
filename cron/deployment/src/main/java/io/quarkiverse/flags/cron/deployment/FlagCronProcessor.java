package io.quarkiverse.flags.cron.deployment;

import io.quarkiverse.flags.cron.CronFlagEvaluator;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;

public class FlagCronProcessor {

    @BuildStep
    void beans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(new AdditionalBeanBuildItem(CronFlagEvaluator.class));
    }

}
