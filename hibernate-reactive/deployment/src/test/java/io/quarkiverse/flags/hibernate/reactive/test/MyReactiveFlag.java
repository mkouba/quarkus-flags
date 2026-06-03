package io.quarkiverse.flags.hibernate.reactive.test;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import io.quarkiverse.flags.hibernate.FlagFeature;
import io.quarkiverse.flags.hibernate.FlagMetadata;
import io.quarkiverse.flags.hibernate.FlagSource;
import io.quarkiverse.flags.hibernate.FlagValue;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

@FlagSource
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
