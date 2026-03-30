package io.quarkiverse.flags.hibernate.reactive.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.common.FlagDefinition;
import io.quarkiverse.flags.hibernate.common.FlagFeature;
import io.quarkiverse.flags.hibernate.common.FlagMetadata;
import io.quarkiverse.flags.hibernate.common.FlagValue;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

@FlagDefinition
@Entity
public class MyReactiveFlag extends PanacheEntity {

    @FlagFeature
    public String feature;

    @FlagValue
    public String value;

    @FlagMetadata
    @ElementCollection
    @CollectionTable
    public Map<String, String> metadata;

}
