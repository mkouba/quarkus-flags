package io.quarkiverse.flags.deployment;

import java.util.Map;

import org.jboss.jandex.Declaration;

import io.quarkus.builder.item.MultiBuildItem;

final class RegisteredFlagBuildItem extends MultiBuildItem {

    private final Declaration declaration;
    private final String flagName;
    private final Map<String, String> metadata;

    RegisteredFlagBuildItem(Declaration declaration, String flagName,
            Map<String, String> metadata) {
        this.declaration = declaration;
        this.flagName = flagName;
        this.metadata = metadata;
    }

    Declaration getDeclaration() {
        return declaration;
    }

    String getFlagName() {
        return flagName;
    }

    Map<String, String> getMetadata() {
        return metadata;
    }
}
