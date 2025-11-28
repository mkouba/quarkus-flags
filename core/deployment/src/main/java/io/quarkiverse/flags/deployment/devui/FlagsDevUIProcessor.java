package io.quarkiverse.flags.deployment.devui;

import io.quarkiverse.flags.runtime.dev.ui.FlagsJsonRPCService;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class FlagsDevUIProcessor {

    @BuildStep(onlyIf = IsLocalDevelopment.class)
    void page(BuildProducer<CardPageBuildItem> cardPages) {

        CardPageBuildItem pageBuildItem = new CardPageBuildItem();

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:toggle-on")
                .componentLink("qwc-flags.js"));

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:box")
                .componentLink("qwc-providers.js"));

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:calculator")
                .componentLink("qwc-evaluators.js"));

        cardPages.produce(pageBuildItem);
    }

    @BuildStep
    JsonRPCProvidersBuildItem rpcProvider() {
        return new JsonRPCProvidersBuildItem(FlagsJsonRPCService.class);
    }

}
