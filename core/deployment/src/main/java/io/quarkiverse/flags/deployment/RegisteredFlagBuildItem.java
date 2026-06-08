package io.quarkiverse.flags.deployment;

import java.util.Map;

import org.jboss.jandex.Declaration;

import io.quarkus.builder.item.MultiBuildItem;

final class RegisteredFlagBuildItem extends MultiBuildItem {

    private final Declaration declaration;
    private final String flagName;
    private final String evaluator;
    private final Map<String, String> metadata;

    RegisteredFlagBuildItem(Declaration declaration, String flagName, String evaluator,
            Map<String, String> metadata) {
        this.declaration = declaration;
        this.flagName = flagName;
        this.evaluator = evaluator;
        this.metadata = metadata;
    }

    Declaration getDeclaration() {
        return declaration;
    }

    String getFlagName() {
        return flagName;
    }

    String getEvaluator() {
        return evaluator;
    }

    Map<String, String> getMetadata() {
        return metadata;
    }
}
